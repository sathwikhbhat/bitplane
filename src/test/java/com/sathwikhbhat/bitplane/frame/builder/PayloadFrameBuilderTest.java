package com.sathwikhbhat.bitplane.frame.builder;

import static org.assertj.core.api.Assertions.assertThat;

import com.sathwikhbhat.bitplane.constants.Constants;
import com.sathwikhbhat.bitplane.frame.model.PayloadFrame;
import java.util.List;
import org.junit.jupiter.api.Test;

class PayloadFrameBuilderTest {

    private final PayloadFrameBuilder builder = new PayloadFrameBuilder();

    @Test
    void build_smallPayload_returnsSingleFrame() {
        List<PayloadFrame> frames = builder.build(new byte[] {1, 2, 3});

        assertThat(frames).hasSize(1);
        assertThat(frames.getFirst().frameIndex()).isZero();
    }

    @Test
    void build_largePayload_returnsMultipleFrames() {
        int maxPayload = Constants.FRAME_BYTE_CAPACITY - Integer.BYTES;
        byte[] data = new byte[maxPayload + 1];

        List<PayloadFrame> frames = builder.build(data);

        assertThat(frames).hasSize(2);
        assertThat(frames.get(1).frameIndex()).isEqualTo(1);
    }
}
