package me.eldodebug.soar.management.online;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import java.net.URI;
import java.util.concurrent.ThreadFactory;

final class PresenceTransport {
    interface Listener {
        void onText(long generation, String text);
        void onClosed(long generation, String reason);
    }
    private final EventLoopGroup loops = new NioEventLoopGroup(1, (ThreadFactory) runnable -> {
        Thread thread = new Thread(runnable, "Soar Presence IO"); thread.setDaemon(true); return thread;
    });
    private final Listener listener;
    private volatile Channel channel;
    private volatile boolean stopped;
    PresenceTransport(Listener listener) { this.listener = listener; }

    void connect(URI endpoint, long generation) {
        if (stopped) return;
        int port = endpoint.getPort() < 0 ? ("wss".equals(endpoint.getScheme()) ? 443 : 80) : endpoint.getPort();
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                endpoint, WebSocketVersion.V13, null, false, new DefaultHttpHeaders(), 65536);
        final SSLEngine engine;
        try {
            if ("wss".equals(endpoint.getScheme())) {
                engine = SSLContext.getDefault().createSSLEngine(endpoint.getHost(), port);
                engine.setUseClientMode(true);
                SSLParameters parameters = engine.getSSLParameters();
                parameters.setEndpointIdentificationAlgorithm("HTTPS");
                engine.setSSLParameters(parameters);
            } else engine = null;
        } catch (Exception e) { listener.onClosed(generation, "TLS initialization failed"); return; }
        Bootstrap bootstrap = new Bootstrap().group(loops).channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.WRITE_BUFFER_HIGH_WATER_MARK, 65536)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override protected void initChannel(SocketChannel ch) {
                        if (engine != null) ch.pipeline().addLast(new SslHandler(engine));
                        ch.pipeline().addLast(new HttpClientCodec(), new HttpObjectAggregator(8192));
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<Object>() {
                            private boolean finished;
                            private void close(String reason) {
                                if (!finished) { finished = true; listener.onClosed(generation, reason); }
                            }
                            private void beginWebSocketHandshake(ChannelHandlerContext ctx) {
                                if (finished || !ctx.channel().isActive()) return;
                                handshaker.handshake(ctx.channel()).addListener(result -> {
                                    if (!result.isSuccess()) { close("WebSocket handshake failed"); ctx.close(); }
                                });
                                ctx.executor().schedule(() -> {
                                    if (!handshaker.isHandshakeComplete()) { close("WebSocket handshake timed out"); ctx.close(); }
                                }, 15, java.util.concurrent.TimeUnit.SECONDS);
                            }
                            @Override public void channelActive(ChannelHandlerContext ctx) {
                                SslHandler tls = ctx.pipeline().get(SslHandler.class);
                                if (tls == null) { beginWebSocketHandshake(ctx); return; }
                                tls.handshakeFuture().addListener(result -> {
                                    if (result.isSuccess()) beginWebSocketHandshake(ctx);
                                    else { close("TLS handshake failed"); ctx.close(); }
                                });
                            }
                            @Override protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
                                if (!handshaker.isHandshakeComplete()) {
                                    handshaker.finishHandshake(ctx.channel(), (FullHttpResponse) msg);
                                    ctx.pipeline().addBefore(ctx.name(), "presence-frame-limit", new WebSocketFrameAggregator(65536));
                                    return;
                                }
                                if (msg instanceof TextWebSocketFrame) {
                                    listener.onText(generation, ((TextWebSocketFrame) msg).text());
                                } else if (msg instanceof PingWebSocketFrame) {
                                    ctx.writeAndFlush(new PongWebSocketFrame(((PingWebSocketFrame) msg).content().retain()));
                                } else if (msg instanceof CloseWebSocketFrame) ctx.close();
                                else if (!(msg instanceof PongWebSocketFrame)) { close("Invalid server frame"); ctx.close(); }
                            }
                            @Override public void channelInactive(ChannelHandlerContext ctx) { close("Disconnected"); }
                            @Override public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                close("Connection failed"); ctx.close(); // Do not log authentication payloads.
                            }
                        });
                    }
                });
        ChannelFuture future = bootstrap.connect(endpoint.getHost(), port);
        channel = future.channel();
        future.addListener(result -> {
            if (!result.isSuccess()) listener.onClosed(generation, "Connection failed");
        });
    }
    void send(String text) {
        Channel current = channel;
        if (current != null && current.isActive() && current.isWritable()) current.writeAndFlush(new TextWebSocketFrame(text));
    }
    void close() {
        Channel current = channel; channel = null;
        if (current != null) current.close();
    }
    void shutdown() { stopped = true; close(); loops.shutdownGracefully(); }
}
