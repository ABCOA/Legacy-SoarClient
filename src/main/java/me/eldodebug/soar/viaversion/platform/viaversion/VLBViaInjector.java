package me.eldodebug.soar.viaversion.platform.viaversion;

import com.viaversion.viaversion.api.platform.ViaInjector;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.libs.fastutil.ints.IntLinkedOpenHashSet;
import com.viaversion.viaversion.libs.fastutil.ints.IntSortedSet;
import com.viaversion.viaversion.libs.gson.JsonObject;

import me.eldodebug.soar.viaversion.netty.VLBPipeline;

public class VLBViaInjector implements ViaInjector {

    @Override
    public void inject() {
    }

    @Override
    public void uninject() {
    }

    @Override
    public String getDecoderName() {
        return VLBPipeline.VIA_DECODER_HANDLER_NAME;
    }

    @Override
    public String getEncoderName() {
        return VLBPipeline.VIA_ENCODER_HANDLER_NAME;
    }

    @Override
    public IntSortedSet getServerProtocolVersions() {
        final IntSortedSet versions = new IntLinkedOpenHashSet();
        for (ProtocolVersion value : ProtocolVersion.getProtocols()) {
            if (value.getVersion() >= ProtocolVersion.v1_7_1.getVersion()) {
                versions.add(value.getVersion());
            }
        }

        return versions;
    }

    @Override
    public int getServerProtocolVersion() {
        return this.getServerProtocolVersions().firstInt();
    }

    @Override
    public JsonObject getDump() {
        return new JsonObject();
    }
}
