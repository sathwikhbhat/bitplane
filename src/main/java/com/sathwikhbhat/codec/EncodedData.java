package com.sathwikhbhat.codec;

import java.awt.image.BufferedImage;
import java.util.List;

public record EncodedData(BufferedImage metadataImage, List<BufferedImage> payloadImages) {
}
