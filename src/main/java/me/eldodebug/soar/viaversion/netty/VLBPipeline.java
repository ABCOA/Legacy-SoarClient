package me.eldodebug.soar.viaversion.netty;

import com.viaversion.viaversion.api.connection.UserConnection;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import me.eldodebug.soar.viaversion.netty.event.CompressionReorderEvent;
import me.eldodebug.soar.viaversion.netty.handler.VLBViaDecodeHandler;
import me.eldodebug.soar.viaversion.netty.handler.VLBViaEncodeHandler;

public abstract class VLBPipeline extends ChannelInboundHandlerAdapter {
    public final static String VIA_DECODER_HANDLER_NAME = "via-decoder";
    public final static String VIA_ENCODER_HANDLER_NAME = "via-encoder";

    private final UserConnection user;

    public VLBPipeline(final UserConnection user) {
        this.user = user;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        super.handlerAdded(ctx);

        ctx.pipeline().addBefore(getDecoderHandlerName(), VIA_DECODER_HANDLER_NAME, this.createVLBViaDecodeHandler());
        ctx.pipeline().addBefore(getEncoderHandlerName(), VIA_ENCODER_HANDLER_NAME, this.createVLBViaEncodeHandler());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);

        if (evt instanceof CompressionReorderEvent) {
            final int decoderIndex = ctx.pipeline().names().indexOf(getDecompressionHandlerName());
            if (decoderIndex == -1) return;

            if (decoderIndex > ctx.pipeline().names().indexOf(VIA_DECODER_HANDLER_NAME)) {
                final ChannelHandler decoder = ctx.pipeline().get(VIA_DECODER_HANDLER_NAME);
                final ChannelHandler encoder = ctx.pipeline().get(VIA_ENCODER_HANDLER_NAME);

                ctx.pipeline().remove(decoder);
                ctx.pipeline().remove(encoder);

                ctx.pipeline().addAfter(getDecompressionHandlerName(), VIA_DECODER_HANDLER_NAME, decoder);
                ctx.pipeline().addAfter(getCompressionHandlerName(), VIA_ENCODER_HANDLER_NAME, encoder);
            }
        }
    }

    public VLBViaDecodeHandler createVLBViaDecodeHandler() {
        return new VLBViaDecodeHandler(this.user);
    }

    public VLBViaEncodeHandler createVLBViaEncodeHandler() {
        return new VLBViaEncodeHandler(this.user);
    }

    public abstract String getDecoderHandlerName();

    public abstract String getEncoderHandlerName();

    public abstract String getDecompressionHandlerName();

    public abstract String getCompressionHandlerName();

    public UserConnection getUser() {
        return user;
    }
}
