package org.ajantis.vmbix;

import io.netty.buffer.BufType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import org.ajantis.vmbix.service.VISphereManager;

import java.nio.charset.Charset;

/**
 * Copyright iFunSoftware 2013
 * @author Dmitry Ivanov
 */
public class VMMonChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final StringDecoder DECODER = new StringDecoder(Charset.forName("UTF8"));
    private static final StringEncoder ENCODER = new StringEncoder(BufType.BYTE);
    private final VISphereManager service;


    public VMMonChannelInitializer(VISphereManager service){
        this.service = service;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("timeout", new IdleStateHandler(0, 110, 0){
            public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent edx){
                ctx.channel().close();
            }
        });

        // Add the text line codec combination first,
        pipeline.addLast("framer", new DelimiterBasedFrameDecoder(
                8192, Delimiters.lineDelimiter()));
        // the encoder and decoder are static as these are sharable
        pipeline.addLast("decoder", DECODER);
        pipeline.addLast("encoder", ENCODER);

        pipeline.addLast("handler", new VIMonServerHandler(service));
    }
}