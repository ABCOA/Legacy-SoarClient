package me.eldodebug.soar.viaversion.netty.handler;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelEncoderException;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

@ChannelHandler.Sharable
public class VLBViaEncodeHandler extends MessageToMessageEncoder<ByteBuf> {
    private final UserConnection user;

    public VLBViaEncodeHandler(UserConnection user) {
        this.user = user;
    }

    @Override
    protected void encode(final ChannelHandlerContext ctx, ByteBuf bytebuf, List<Object> out) throws Exception {
        if (!user.checkOutgoingPacket()) throw CancelEncoderException.generate(null);
        if (!user.shouldTransformPacket()) {
            out.add(bytebuf.retain());
            return;
        }

        ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(bytebuf);
        try {
            user.transformOutgoing(transformedBuf, CancelEncoderException::generate);

            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (PipelineUtil.containsCause(cause, CancelCodecException.class)) return;
        super.exceptionCaught(ctx, cause);
    }
}
