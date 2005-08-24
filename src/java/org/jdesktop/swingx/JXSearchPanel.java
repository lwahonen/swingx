/*
 * $Id$
 *
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 */

package org.jdesktop.swingx;

import java.awt.Container;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jdesktop.swingx.action.AbstractActionExt;
import org.jdesktop.swingx.action.ActionContainerFactory;
import org.jdesktop.swingx.action.BoundAction;
import org.jdesktop.swingx.decorator.PatternFilter;
import org.jdesktop.swingx.decorator.PatternHighlighter;
import org.jdesktop.swingx.decorator.PatternMatcher;
import org.jdesktop.swingx.plaf.LookAndFeelAddons;
import org.jdesktop.swingx.plaf.SearchPanelAddon;

/**
 * Rudimentary search panel.
 * 
 * Updates PatternMatchers from user input.
 * 
 * Supports 
 * 
 * <ol>
 * <li> text input to match
 * <li> match rules like contains/equals/... 
 * <li> toggle case sensitive match
 * </ol>
 * 
 * TODO: allow custom PatternModel and/or access 
 * to configuration of bound PatternModel. 
 * 
 * TODO: fully support control of multiple PatternMatchers.
 * 
 * @author Ramesh Gupta
 * @author Jeanette Winzenburg
 * 
 */
public class JXSearchPanel extends JPanel {
    static {
        // Hack: make sure the resource bundle is loaded
        LookAndFeelAddons.contribute(new SearchPanelAddon());
    }


    public static final String MATCH_RULE_ACTION_COMMAND = "selectMatchRule";

    public static final String MATCH_CASE_ACTION_COMMAND = "matchCase";

    private JLabel fieldName;

    private JCheckBox matchCase;

    private JComboBox searchCriteria;

    private JTextField searchField;

    private List<PatternMatcher> patternMatchers;
    
    private PatternModel patternModel;

    public JXSearchPanel() {
        initActions();
        initComponents();
        build();
        bind();
    }

//----------------- accessing public properties

    /**
     * sets the PatternFilter control.
     * 
     * PENDING: change to do a addPatternMatcher to enable multiple control.
     * 
     */
    public void setPatternFilter(PatternFilter filter) {
        getPatternMatchers().add(filter);
        updateFieldName(filter);
    }

    /**
     * sets the PatternHighlighter control.
     * 
     * PENDING: change to do a addPatternMatcher to enable multiple control.
     * 
     */
    public void setPatternHighlighter(PatternHighlighter highlighter) {
        getPatternMatchers().add(highlighter);
        updateFieldName(highlighter);
//        if (fieldName.getText().length() == 0) { // ugly hack
//            fieldName.setText("Field");
//            /** @todo Remove this hack!!! */
//        }
    }



    /** 
     * returns the patternfilter - really needed?
     * @return
     */
//    public PatternFilter getPatternFilter() {
//        return patternFilter;
//    }

    /** 
     * returns the patternHighlighter - really needed?
     * @return
     */
//    public PatternHighlighter getPatternHighlighter() {
//        return patternHighlighter;
//    }


    /**
     * set the label of the search combo.
     * 
     * @param name
     */
    public void setFieldName(String name) {
        fieldName.setText(name);
    }

    /**
     * returns the label of the search combo.
     * 
     */
    public String getFieldName() {
        return fieldName.getText();
    }

    /**
     * returns the current compiled Pattern.
     * 
     * @return
     */
    public Pattern getPattern() {
        return patternModel.getPattern();
    }


    // ---------------- action callbacks

    /**
     * set's the PatternModel's MatchRule to the selected in combo. 
     * 
     * NOTE: this
     * is public as an implementation artefact! No need to ever call directly.
     */
    public void updateMatchRule() {
        getPatternModel().setMatchRule(
                (String) searchCriteria.getSelectedItem());
    }

    //---------------- init actions
    
    private void initActions() {
        getActionMap().put(MATCH_RULE_ACTION_COMMAND,
                createSelectMatchRuleAction());
        getActionMap().put(MATCH_CASE_ACTION_COMMAND, createMatchCaseAction());
    }

    /**
     * tries to find a String value from the UIManager, prefixing the
     * given key with the UIPREFIX. 
     * 
     * TODO: move to utilities?
     * 
     * @param key 
     * @return the String as returned by the UIManager or key if the returned
     *   value was null.
     */
    private String getUIString(String key) {
        String text = UIManager.getString(PatternModel.SEARCH_PREFIX + key);
        return text != null ? text : key;
    }

    /**
     * 
     * @return
     */
    private AbstractActionExt createMatchCaseAction() {
        String actionName = getUIString(MATCH_CASE_ACTION_COMMAND);
        BoundAction action = new BoundAction(actionName,
                MATCH_CASE_ACTION_COMMAND);
        action.setStateAction();
        action.registerCallback(getPatternModel(), "setCaseSensitive");
        action.setSelected(getPatternModel().isCaseSensitive());
        return action;
    }

    /**
     * return the Action to select the Match Rule.
     * 
     * @return
     */
    private AbstractActionExt createSelectMatchRuleAction() {
        String actionName = getUIString(MATCH_RULE_ACTION_COMMAND);
        BoundAction action = new BoundAction(actionName,
                MATCH_RULE_ACTION_COMMAND);
        action.registerCallback(this, "updateMatchRule");
        return action;
    }

    
    //------------------ support synch the model <--> components
    
    /**
     * 
     */
    private PatternModel getPatternModel() {
        if (patternModel == null) {
            patternModel = new PatternModel();
            patternModel.addPropertyChangeListener(getPatternModelListener());
        }
        return patternModel;
    }

    /**
     * creates and returns a PropertyChangeListener to the PatternModel.
     * 
     * NOTE: the patternModel is totally under control of this class - currently
     * there's no need to keep a reference to the listener.
     * 
     * @return
     */
    private PropertyChangeListener getPatternModelListener() {
        PropertyChangeListener l = new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if ("pattern".equals(evt.getPropertyName())) {
                    refreshPatternMatchersFromModel();
                }

            }

        };
        return l;
    }

    /**
     * callback method from listening to PatternModel.
     *
     */
    protected void refreshPatternMatchersFromModel() {
        Pattern pattern = getPattern();

        for (Iterator<PatternMatcher> iter = getPatternMatchers().iterator(); iter.hasNext();) {
            iter.next().setPattern(pattern);
            
        }
    }

    private DocumentListener getSearchFieldListener() {
        DocumentListener l = new DocumentListener() {
            public void changedUpdate(DocumentEvent ev) {
                // JW - really?? we've a PlainDoc without Attributes
                refreshModelFromDocument();
            }

            public void insertUpdate(DocumentEvent ev) {
                refreshModelFromDocument();
            }

            public void removeUpdate(DocumentEvent ev) {
                refreshModelFromDocument();
            }

        };
        return l;
    }

    /**
     * callback method from listening to searchField.
     *
     */
    protected void refreshModelFromDocument() {
        getPatternModel().setRawText(searchField.getText());
    }

    /**
     * @param filter
     */
    private void updateFieldName(PatternMatcher matcher) {
        if (matcher instanceof PatternFilter) {
            PatternFilter filter = (PatternFilter) matcher;
            if (filter == null) {
                fieldName.setText("Field");
            } else {
                fieldName.setText(filter.getColumnName());
            }
        } else {
            if (fieldName.getText().length() == 0) { // ugly hack
                fieldName.setText("Field");
                /** @todo Remove this hack!!! */
            }

        }
    }

    private List<PatternMatcher> getPatternMatchers() {
        if (patternMatchers == null) {
            patternMatchers = new ArrayList<PatternMatcher>();
        }
        return patternMatchers;
    }

    //--------------------- binding support
    
    /**
     * bind the components to the patternModel/actions.
     */
    private void bind() {
        List matchRules = getPatternModel().getMatchRules();
        // PENDING: map rules to localized strings
        ComboBoxModel model = new DefaultComboBoxModel(matchRules.toArray());
        model.setSelectedItem(getPatternModel().getMatchRule());
        searchCriteria.setModel(model);
        searchCriteria.setAction(getActionMap().get(MATCH_RULE_ACTION_COMMAND));
        searchField.getDocument().addDocumentListener(getSearchFieldListener());
        // bind the action should be decoupled from creating the button!
        ActionContainerFactory factory = new ActionContainerFactory(null);
        factory.configureButton(matchCase, 
                (AbstractActionExt) getActionMap().get(MATCH_CASE_ACTION_COMMAND),
                null);
        
    }
    


    //------------------------ init ui
    
    /**
     * build container by adding all components.
     * PRE: all components created.
     */
    private void build() {
        searchField.setPreferredSize(new Dimension(80, 20));
        add(fieldName);
        add(searchCriteria);
        add(searchField);
        add(matchCase);
    }

    /**
     * create contained components.
     * 
     * PENDING: we should create all components we want to 
     * access later here. Currently that's not quite possible because
     * of ActionContainerFactory limitation (doesn't support config of
     * action components after creation)
     *
     */
    private void initComponents() {
        fieldName = new JLabel();
        searchField = new JTextField();
        searchCriteria = new JComboBox();
        matchCase = new JCheckBox();
    }


}
