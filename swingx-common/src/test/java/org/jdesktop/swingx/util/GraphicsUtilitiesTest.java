package org.jdesktop.swingx.util;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;

import org.junit.Ignore;
import org.junit.Test;

public class GraphicsUtilitiesTest {
	@Test
	@Ignore("this test fails for image type other than TYPE_INT_(A)RGB, e.g., when run on Travis")
	public void testClear() {
		BufferedImage img = GraphicsUtilities.createCompatibleImage(1, 1);
		GraphicsUtilities.clear(img);
		assertEquals(0, GraphicsUtilities.getPixels(img, 0, 0, 1, 1, null)[0]);
	}
}
