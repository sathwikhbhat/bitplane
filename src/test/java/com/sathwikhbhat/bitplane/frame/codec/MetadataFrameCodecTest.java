package com.sathwikhbhat.bitplane.frame.codec;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sathwikhbhat.bitplane.frame.model.MetadataFrame;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.junit.jupiter.api.Test;

class MetadataFrameCodecTest {

    private final MetadataFrameCodec codec = new MetadataFrameCodec();

    @Test
    void serializeDeserialize_roundtrip() throws IOException {
        MetadataFrame frame = new MetadataFrame("file.txt", 1024L, 3);

        MetadataFrame result = codec.deserialize(codec.serialize(frame));

        assertThat(result.fileName()).isEqualTo("file.txt");
        assertThat(result.fileSize()).isEqualTo(1024L);
        assertThat(result.totalPayloadFrames()).isEqualTo(3);
    }

    @Test
    void deserialize_negativeLength_throwsIllegalArgumentException() {
        byte[] data = ByteBuffer.allocate(4).putInt(-1).array();

        assertThatThrownBy(() -> codec.deserialize(data)).isInstanceOf(IllegalArgumentException.class);
    }
}
