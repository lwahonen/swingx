/*
 * Created on 04.10.2010
 *
 */
package org.jdesktop.swingx;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class JXFrameTest extends InteractiveTestCase {
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(JXFrameTest.class
            .getName());
    
    @Test
    public void testGraphicsConfig() {
        // This test will not work in a headless configuration.
        if (GraphicsEnvironment.isHeadless()) {
            LOG.fine("cannot run ui test - headless environment");
            return;
        }
        JXFrame compare = new JXFrame();
        GraphicsConfiguration gc = compare.getGraphicsConfiguration();
        JXFrame frame = new JXFrame(gc);
        assertEquals(gc, frame.getGraphicsConfiguration());
        assertEquals(compare.getDefaultCloseOperation(), frame.getDefaultCloseOperation());
        assertEquals(compare.getTitle(), frame.getTitle());
    }    
}
