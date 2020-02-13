/*
 * Created on 28.03.2006
 *
 */
package org.jdesktop.swingx.hyperlink;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Logger;

import javax.swing.Action;

import junit.framework.TestCase;

import org.jdesktop.swingx.hyperlink.HyperlinkAction.URIVisitor;
import org.jdesktop.test.PropertyChangeReport;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.Matchers.any;

/**
 *
 * @author Jeanette Winzenburg, Berlin
 */
@RunWith(JUnit4.class)
public class HyperlinkActionTest extends TestCase {

    @SuppressWarnings("unused")
    private static final Logger LOG = Logger
            .getLogger(HyperlinkActionTest.class.getName());

    private PropertyChangeReport report;

    @Before
    public void setUpJ4() throws Exception {
        setUp();
    }

    @After
    public void tearDownJ4() throws Exception {
        tearDown();
    }

    /**
     * Issue #1227-swingx: HyperlinkAction - update visited property
     * 
     * @throws URISyntaxException
     */
    @Test
    public void testVisited() throws Exception {
        // This test will not work in a headless configuration.
        if (GraphicsEnvironment.isHeadless()) {
            LOG.fine("cannot run ui test - headless environment");
            return;
        }
        final URI uri = new URI("http://localhost");
        final HyperlinkAction action = HyperlinkAction
                .createHyperlinkAction(uri);
        final URIVisitor visitor = mock(URIVisitor.class);
        when(visitor.isEnabled(any())).thenReturn(true);
        action.setURIVisitor(visitor);

        action.actionPerformed(null);
        assertEquals(true, action.isVisited());
        verify(visitor).visit(uri);
    }

    @Test
    @Ignore("works locally, but not on Travis")
    public void testURIActionFactoryMail() throws URISyntaxException {
        // This test will not work in a headless configuration.
        if (GraphicsEnvironment.isHeadless()) {
            LOG.fine("cannot run ui test - headless environment");
            return;
        }
        final URI uri = new URI("mailto:java-net@java.sun.com");
        final HyperlinkAction action = HyperlinkAction
                .createHyperlinkAction(uri);
        assertEquals(true, action.isEnabled());
        assertEquals(Desktop.Action.MAIL, action.getDesktopAction());
    }

    @Test
    public void testURIActionFactoryBrowseNull() {
        // This test will not work in a headless configuration.
        if (GraphicsEnvironment.isHeadless()) {
            LOG.fine("cannot run ui test - headless environment");
            return;
        }

        final HyperlinkAction action = HyperlinkAction
                .createHyperlinkAction(null);
        assertEquals(false, action.isEnabled());
        assertEquals(Desktop.Action.BROWSE, action.getDesktopAction());
    }

    @Test
    public void testURIActionPerformed() {
        // This test will not work in a headless configuration.
        if (GraphicsEnvironment.isHeadless()) {
            LOG.fine("cannot run ui test - headless environment");
            return;
        }

        final HyperlinkAction action = new HyperlinkAction();
        action.actionPerformed(null);
    }

    @Test
    @Ignore("works locally, but not on Travis")
    public void testURIActionNullMailEnabled() {
        // This test will not work in a headless configuration.
        if (GraphicsEnvironment.isHeadless()) {
            LOG.fine("cannot run ui test - headless environment");
            return;
        }

        final HyperlinkAction action = new HyperlinkAction(Desktop.Action.MAIL);
        assertEquals(true, action.isEnabled());
    }

    @Test
    public void testURIActionNullTargetBrowseDisabled() {
        // This test will not work in a headless configuration.
        if (GraphicsEnvironment.isHeadless()) {
            LOG.fine("cannot run ui test - headless environment");
            return;
        }

        final HyperlinkAction action = new HyperlinkAction();
        assertEquals(false, action.isEnabled());
    }

    @Test
    public void testUriActionEmptyConstructor() {
        // This test will not work in a headless configuration.
        if (GraphicsEnvironment.isHeadless()) {
            LOG.fine("cannot run ui test - headless environment");
            return;
        }
        final HyperlinkAction action = new HyperlinkAction();
        assertEquals(Desktop.Action.BROWSE, action.getDesktopAction());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testUriActionIllegalType() {
        // This test will not work in a headless configuration.
        if (GraphicsEnvironment.isHeadless()) {
            LOG.fine("cannot run ui test - headless environment");
            throw new IllegalArgumentException("dummy");
        }
        new HyperlinkAction(Desktop.Action.EDIT);
    }

    /**
     * test if auto-installed visited property is respected.
     *
     */
    @Test
    public void testConstructorsAndCustomTargetInstall() {
        final Object target = new Object();
        final boolean visitedIsTrue = true;
        final AbstractHyperlinkAction<Object> linkAction = new AbstractHyperlinkAction<Object>(
                target) {

            @Override
            public void actionPerformed(final ActionEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            protected void installTarget() {
                super.installTarget();
                setVisited(visitedIsTrue);
            }

        };
        assertEquals(visitedIsTrue, linkAction.isVisited());

    }

    /**
     * test constructors with parameters
     *
     */
    @Test
    public void testConstructors() {
        final Object target = new Object();
        final AbstractHyperlinkAction<Object> linkAction = new AbstractHyperlinkAction<Object>(
                target) {

            @Override
            public void actionPerformed(final ActionEvent e) {
                // TODO Auto-generated method stub

            }

        };
        assertEquals(target, linkAction.getTarget());
        assertFalse(linkAction.isVisited());
    }

    /**
     * test visited/target properties of LinkAction.
     *
     */
    @Test
    public void testLinkAction() {
        final AbstractHyperlinkAction<Object> linkAction = new AbstractHyperlinkAction<Object>(
                null) {

            @Override
            public void actionPerformed(final ActionEvent e) {
                // TODO Auto-generated method stub

            }

        };
        linkAction.addPropertyChangeListener(report);

        final boolean visited = linkAction.isVisited();
        assertFalse(visited);
        linkAction.setVisited(!visited);
        assertEquals(!visited, linkAction.isVisited());
        assertEquals(1,
                report.getEventCount(AbstractHyperlinkAction.VISITED_KEY));

        report.clear();
        // testing target property
        assertNull(linkAction.getTarget());
        final Object target = new Object();
        linkAction.setTarget(target);
        assertEquals(target, linkAction.getTarget());
        assertEquals(1, report.getEventCount("target"));
        // testing documented default side-effects of un/installTarget
        assertEquals(target.toString(), linkAction.getName());
        assertFalse(linkAction.isVisited());
        assertEquals(1, report.getEventCount(Action.NAME));
        assertEquals(1,
                report.getEventCount(AbstractHyperlinkAction.VISITED_KEY));
        // fired the expected events only.
        assertEquals(3, report.getEventCount());
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        report = new PropertyChangeReport();
    }

}
