package com.sathwikhbhat.bitplane.frame.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sathwikhbhat.bitplane.frame.model.PayloadFrame;
import org.junit.jupiter.api.Test;

class PayloadFrameCodecTest {

    private final PayloadFrameCodec codec = new PayloadFrameCodec();

    @Test
    void serializeDeserialize_roundtrip() {
        PayloadFrame frame = new PayloadFrame(5, new byte[] {1, 2, 3});

        PayloadFrame result = codec.deserialize(codec.serialize(frame));

        assertThat(result.frameIndex()).isEqualTo(5);
        assertThat(result.payload()).isEqualTo(new byte[] {1, 2, 3});
    }

    @Test
    void deserialize_tooShort_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> codec.deserialize(new byte[0])).isInstanceOf(IllegalArgumentException.class);
    }
}
