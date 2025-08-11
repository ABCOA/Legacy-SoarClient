package me.eldodebug.soar.management.music.openal;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;

public class OpenALMusicPlayer {

    public interface VisualizerListener {
        void onMagnitudes(float[] mags);
    }

    private int source = 0;
    private int[] buffers;
    private volatile boolean stopRequested = false;
    private volatile boolean paused = false;
    private Thread streamThread;

    private int sampleRate = 44100;
    private int channels = 2;
    private long samplesDecoded = 0;      // 估算播放秒数
    private float volume = 1.0f;

    private VisualizerListener visualizerListener;
    private Runnable onFinished;

    private static final int BUFFER_MS = 200;
    private static final int NUM_BUFFERS = 4;

    public void setVisualizerListener(VisualizerListener l) { this.visualizerListener = l; }
    public void setOnFinished(Runnable r) { this.onFinished = r; }

    public synchronized void playMp3(File file) throws Exception {
        stop();

        ensureAL();

        source  = AL10.alGenSources();
        buffers = new int[NUM_BUFFERS];
        for (int i = 0; i < NUM_BUFFERS; i++) buffers[i] = AL10.alGenBuffers();

        stopRequested = false;
        paused = false;
        samplesDecoded = 0;

        streamThread = new Thread(() -> {
            try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
                Bitstream bitstream = new Bitstream(in);
                Decoder decoder = new Decoder();

                Header h0 = bitstream.readFrame();
                if (h0 == null) throw new EOFException("Empty mp3");
                SampleBuffer sb0 = (SampleBuffer) decoder.decodeFrame(h0, bitstream);
                sampleRate = sb0.getSampleFrequency();
                channels   = sb0.getChannelCount();
                bitstream.closeFrame();

                int samplesPerBuffer = (int)((sampleRate * BUFFER_MS) / 1000.0) * channels;

                for (int i = 0; i < NUM_BUFFERS; i++) {
                    ByteBuffer pcm = decodeToBuffer(bitstream, decoder, samplesPerBuffer);
                    if (pcm == null) break;
                    queueBuffer(buffers[i], pcm);
                }

                AL10.alSourcef(source, AL10.AL_GAIN, volume);
                AL10.alSourcePlay(source);

                while (!stopRequested) {
                    if (paused) { sleep(10); continue; }

                    int processed = AL10.alGetSourcei(source, AL10.AL_BUFFERS_PROCESSED);
                    while (processed-- > 0) {
                        int buf = AL10.alSourceUnqueueBuffers(source);
                        ByteBuffer pcm = decodeToBuffer(bitstream, decoder, samplesPerBuffer);
                        if (pcm != null) {
                            queueBuffer(buf, pcm);
                        } else {
                            if (AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED) == 0) {
                                stopRequested = true;
                            }
                        }
                    }

                    if (AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) != AL10.AL_PLAYING
                            && AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED) > 0) {
                        AL10.alSourcePlay(source);
                    }

                    sleep(5);
                }

                AL10.alSourceStop(source);
                bitstream.close();
            } catch (Throwable t) {
                t.printStackTrace();
            } finally {
                cleanupALObjects();
                if (onFinished != null) try { onFinished.run(); } catch (Throwable ignored) {}
            }
        }, "OpenAL-MP3-Streamer");

        streamThread.setDaemon(true);
        streamThread.start();
    }

    public synchronized void pauseResume() {
        if (source == 0) return;
        paused = !paused;
        if (paused) AL10.alSourcePause(source);
        else AL10.alSourcePlay(source);
    }

    public synchronized void stop() {
        stopRequested = true;
        if (streamThread != null) {
            try { streamThread.join(200); } catch (InterruptedException ignored) {}
            streamThread = null;
        }
        cleanupALObjects();
    }

    public boolean isPlaying() {
        if (source == 0 || paused) return false;
        return AL10.alGetSourcei(source, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
    }

    public void setVolume(float v) {
        volume = Math.max(0f, Math.min(1f, v));
        if (source != 0) AL10.alSourcef(source, AL10.AL_GAIN, volume);
    }

    public void setVolume(double v) {
        setVolume((float) v);
    }

    public float getCurrentTimeSec() { return samplesDecoded / (float) sampleRate; }


    private void ensureAL() throws Exception {
        if (!AL.isCreated()) {
            try { AL.create(); } catch (Throwable ignored) { /* MC 已创建时会失败，忽略 */ }
        }
    }

    private void queueBuffer(int bufferId, ByteBuffer pcm) {
        int format = (channels == 1) ? AL10.AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16;
        AL10.alBufferData(bufferId, format, pcm, sampleRate);
        AL10.alSourceQueueBuffers(source, bufferId);
    }

    private ByteBuffer decodeToBuffer(Bitstream bitstream, Decoder decoder, int samplesPerBuffer) throws Exception {
        ByteBuffer out = BufferUtils.createByteBuffer(samplesPerBuffer * 2); // 16-bit
        int written = 0;

        while (written < samplesPerBuffer) {
            Header h = bitstream.readFrame();
            if (h == null) break;
            SampleBuffer sb = (SampleBuffer) decoder.decodeFrame(h, bitstream);

            short[] data = sb.getBuffer();
            int len = Math.min(data.length, samplesPerBuffer - written);

            for (int i = 0; i < len; i++) out.putShort(data[i]);
            written += len;
            samplesDecoded += len / channels;

            if (visualizerListener != null) {
                int bands = 100;
                float[] mags = new float[bands];
                int step = Math.max(1, len / bands);
                int idx = 0;
                for (int b = 0; b < bands; b++) {
                    long acc = 0; int cnt = 0;
                    for (int k = 0; k < step && idx < len; k++, idx++) { acc += Math.abs(data[idx]); cnt++; }
                    mags[b] = cnt == 0 ? 0f : (acc / (cnt * 32768f));
                }
                try { visualizerListener.onMagnitudes(mags); } catch (Throwable ignored) {}
            }

            bitstream.closeFrame();
        }

        if (written == 0) return null;
        out.flip();
        return out;
    }

    private void cleanupALObjects() {
        if (source != 0) {
            int queued = AL10.alGetSourcei(source, AL10.AL_BUFFERS_QUEUED);
            while (queued-- > 0) AL10.alSourceUnqueueBuffers(source);
            AL10.alSourceStop(source);
            AL10.alDeleteSources(source);
            source = 0;
        }
        if (buffers != null) {
            for (int b : buffers) AL10.alDeleteBuffers(b);
            buffers = null;
        }
    }

    private static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}
