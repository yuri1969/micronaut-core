/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.http.netty.channel;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.ServerChannel;
import io.netty.channel.kqueue.KQueue;
import io.netty.channel.kqueue.KQueueDatagramChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerDomainSocketChannel;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.unix.ServerDomainSocketChannel;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;

/**
 * Factory for KQueueEventLoopGroup.
 *
 * @author croudet
 */
@Singleton
@Internal
@Requires(classes = KQueue.class, condition = KQueueAvailabilityCondition.class)
@Named(EventLoopGroupFactory.NATIVE)
@BootstrapContextCompatible
public class KQueueEventLoopGroupFactory implements EventLoopGroupFactory {

    /**
     * Creates a KQueueEventLoopGroup.
     *
     * @param threads       The number of threads to use.
     * @param threadFactory The thread factory.
     * @param ioRatio       The io ratio.
     * @return A KQueueEventLoopGroup.
     */
    @Override
    public EventLoopGroup createEventLoopGroup(int threads, ThreadFactory threadFactory, @Nullable Integer ioRatio) {
        return withIoRatio(new KQueueEventLoopGroup(threads, threadFactory), ioRatio);
    }

    /**
     * Creates a KQueueEventLoopGroup.
     *
     * @param threads  The number of threads to use.
     * @param executor An Executor.
     * @param ioRatio  The io ratio.
     * @return A KQueueEventLoopGroup.
     */
    @Override
    public EventLoopGroup createEventLoopGroup(int threads, Executor executor, @Nullable Integer ioRatio) {
        return withIoRatio(new KQueueEventLoopGroup(threads, executor), ioRatio);
    }

    @Override
    public boolean isNative() {
        return true;
    }

    /**
     * Returns the server channel class.
     *
     * @return KQueueServerSocketChannel.
     */
    @Override
    public Class<? extends ServerSocketChannel> serverSocketChannelClass() {
        return KQueueServerSocketChannel.class;
    }

    @Override
    public Class<? extends ServerDomainSocketChannel> domainServerSocketChannelClass() throws UnsupportedOperationException {
        try {
            return KQueueServerDomainSocketChannel.class;
        } catch (NoClassDefFoundError e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @Override
    public KQueueServerSocketChannel serverSocketChannelInstance(EventLoopGroupConfiguration configuration) {
        return new KQueueServerSocketChannel();
    }

    @Override
    public ServerChannel domainServerSocketChannelInstance(@Nullable EventLoopGroupConfiguration configuration) {
        try {
            return new KQueueServerDomainSocketChannel();
        } catch (NoClassDefFoundError e) {
            throw new UnsupportedOperationException(e);
        }
    }

    @NonNull
    @Override
    public Class<? extends SocketChannel> clientSocketChannelClass(@Nullable EventLoopGroupConfiguration configuration) {
        return KQueueSocketChannel.class;
    }

    @Override
    public SocketChannel clientSocketChannelInstance(EventLoopGroupConfiguration configuration) {
        return new KQueueSocketChannel();
    }

    private static KQueueEventLoopGroup withIoRatio(KQueueEventLoopGroup group, @Nullable Integer ioRatio) {
        if (ioRatio != null) {
            group.setIoRatio(ioRatio);
        }
        return group;
    }

    @Override
    public Class<? extends Channel> channelClass(NettyChannelType type) throws UnsupportedOperationException {
        return switch (type) {
            case SERVER_SOCKET -> KQueueServerSocketChannel.class;
            case CLIENT_SOCKET -> KQueueSocketChannel.class;
            case DOMAIN_SERVER_SOCKET -> KQueueServerDomainSocketChannel.class;
            case DATAGRAM_SOCKET -> KQueueDatagramChannel.class;
        };
    }

    @Override
    public Class<? extends Channel> channelClass(NettyChannelType type, @Nullable EventLoopGroupConfiguration configuration) {
        return channelClass(type);
    }

    @Override
    public Channel channelInstance(NettyChannelType type, @Nullable EventLoopGroupConfiguration configuration) {
        return switch (type) {
            case SERVER_SOCKET -> new KQueueServerSocketChannel();
            case CLIENT_SOCKET -> new KQueueSocketChannel();
            case DOMAIN_SERVER_SOCKET -> new KQueueServerDomainSocketChannel();
            case DATAGRAM_SOCKET -> new KQueueDatagramChannel();
        };
    }
}
