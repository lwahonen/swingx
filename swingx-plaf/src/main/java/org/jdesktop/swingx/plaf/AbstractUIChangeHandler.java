package org.jdesktop.swingx.plaf;

import java.beans.PropertyChangeListener;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.JComponent;

public abstract class AbstractUIChangeHandler implements PropertyChangeListener {
	// prevent double installation.
	private final Map<JComponent, Boolean> installed = new WeakHashMap<JComponent, Boolean>();

	public void install(JComponent c) {
		synchronized (installed) {
			if (!isInstalled(c)) {
				c.addPropertyChangeListener("UI", this);
				installed.put(c, Boolean.TRUE);
			}
		}
	}

	public boolean isInstalled(JComponent c) {
		synchronized (installed) {
			return installed.containsKey(c);
		}
	}

	public void uninstall(JComponent c) {
		synchronized (installed) {
			c.removePropertyChangeListener("UI", this);
			installed.remove(c);
		}
	}
}