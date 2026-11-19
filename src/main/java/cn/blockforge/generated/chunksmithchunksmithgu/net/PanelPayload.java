package cn.blockforge.generated.chunksmithchunksmithgu.net;

import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/**
 * 面板的唯一网络载荷类型。
 *
 * <p>上游是 Forge 1.20.1，用 {@code SimpleChannel} 为四个消息类各注册一条通道；
 * NeoForge 21.1 去掉了 SimpleChannel，改由 {@code CustomPacketPayload} + {@code StreamCodec}
 * 表达。这里用一个「id + 原始字节」的信封承载全部四条逻辑消息，四个消息类原有的
 * encode/decode 一个字符都不用改——信封只负责搬运，语义仍由 id 决定。
 *
 * <p>用 {@code byte[]} 而不是直接持有 {@code FriendlyByteBuf}：载荷要能跨线程移交
 * （热力图在扫描线程算完再回主线程发），裸 buffer 的 readerIndex 会被两边同时推进。
 */
public record PanelPayload(int id, byte[] data) implements CustomPacketPayload {

    public static final Type<PanelPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("chunksmith_modern_gui", "panel"));

    public static final StreamCodec<FriendlyByteBuf, PanelPayload> CODEC = StreamCodec.of(
            (buf, payload) -> {
                buf.writeVarInt(payload.id());
                buf.writeVarInt(payload.data().length);
                buf.writeBytes(payload.data());
            },
            buf -> {
                int id = buf.readVarInt();
                int len = buf.readVarInt();
                if (len < 0 || len > 1 << 22) {
                    throw new IllegalArgumentException("bad payload length: " + len);
                }
                byte[] data = new byte[len];
                buf.readBytes(data);
                return new PanelPayload(id, data);
            });

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    /** 把已写好的 buffer 收成信封（读取剩余全部字节）。 */
    public static PanelPayload of(int id, FriendlyByteBuf buf) {
        byte[] data = new byte[buf.readableBytes()];
        buf.readBytes(data);
        return new PanelPayload(id, data);
    }

    /** 取出 buffer 供 decode 使用。 */
    public FriendlyByteBuf buffer() {
        return new FriendlyByteBuf(Unpooled.wrappedBuffer(data));
    }
}
