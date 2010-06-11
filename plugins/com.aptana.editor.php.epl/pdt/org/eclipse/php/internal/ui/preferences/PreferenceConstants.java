/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Zend Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.php.internal.ui.preferences;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;

import com.aptana.editor.php.epl.PHPEplPlugin;

public class PreferenceConstants {

	/**
	 * A named preferences that controls if PHP elements are also sorted by
	 * visibility.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String APPEARANCE_ENABLE_VISIBILITY_SORT_ORDER = "enableVisibilityOrder"; //$NON-NLS-1$

	/**
	 * A named preference that defines how member elements are ordered by
	 * visibility in the PHP views using the <code>PHPElementSorter</code>.
	 * <p>
	 * Value is of type <code>String</code>: A comma separated list of the
	 * following entries. Each entry must be in the list, no duplication. List
	 * order defines the sort order.
	 * <ul>
	 * <li><b>B</b>: Public</li>
	 * <li><b>V</b>: Private</li>
	 * <li><b>R</b>: Protected</li>
	 * <li><b>D</b>: Default</li>
	 * </ul>
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String APPEARANCE_VISIBILITY_SORT_ORDER = "org.eclipse.php.ui.visibility.order"; //$NON-NLS-1$

	/**
	 * A named preference that defines how member elements are ordered by the
	 * PHP views using the <code>PHPElementSorter</code>.
	 * <p>
	 * Value is of type <code>String</code>: A comma separated list of the
	 * following entries. Each entry must be in the list, no duplication. List
	 * order defines the sort order.
	 * <ul>
	 * <li><b>T</b>: Types</li>
	 * <li><b>M</b>: Methods</li>
	 * <li><b>F</b>: Fields</li>
	 * </ul>
	 * </p>
	 */
	public static final String APPEARANCE_MEMBER_SORT_ORDER = "outlinesortoption"; //$NON-NLS-1$

	/**
	 * A named preference that controls return type rendering of methods in the
	 * UI.
	 * <p>
	 * Value is of type <code>Boolean</code>: if <code>true</code> return types
	 * are rendered
	 * </p>
	 */
	public static final String APPEARANCE_METHOD_RETURNTYPE = "methodreturntype";//$NON-NLS-1$

	/**
	 * A named preference that controls type parameter rendering of methods in
	 * the UI.
	 * <p>
	 * Value is of type <code>Boolean</code>: if <code>true</code> return types
	 * are rendered
	 * </p>
	 */
	public static final String APPEARANCE_METHOD_TYPEPARAMETERS = "methodtypeparametesr";//$NON-NLS-1$

	/**
	 * A named preference that controls whether annotation roll over is used or
	 * not.
	 * <p>
	 * Value is of type <code>Boolean</code>. If
	 * <code>true<code> the annotation ruler column
	 * uses a roll over to display multiple annotations
	 * </p>
	 */
	public static final String EDITOR_ANNOTATION_ROLL_OVER = "editor_annotation_roll_over"; //$NON-NLS-1$

	/**
	 * A named preference that controls if correction indicators are shown in
	 * the UI.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public final static String EDITOR_CORRECTION_INDICATION = "PHPEditorShowTemporaryProblem"; //$NON-NLS-1$

	/**
	 * A named preference that controls the smart tab behavior.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * 
	 */
	public static final String EDITOR_SMART_TAB = "smart_tab"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether occurrences are marked in the
	 * editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_MARK_OCCURRENCES = "markOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether occurrences are sticky in the
	 * editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_STICKY_OCCURRENCES = "stickyOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether type occurrences are marked.
	 * Only valid if {@link #EDITOR_MARK_OCCURRENCES} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_MARK_TYPE_OCCURRENCES = "markTypeOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether method occurrences are marked.
	 * Only valid if {@link #EDITOR_MARK_OCCURRENCES} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_MARK_METHOD_OCCURRENCES = "markMethodOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether function occurrences are marked.
	 * Only valid if {@link #EDITOR_MARK_OCCURRENCES} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.4
	 */
	public static final String EDITOR_MARK_FUNCTION_OCCURRENCES = "markFunctionOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether constant (static final)
	 * occurrences are marked. Only valid if {@link #EDITOR_MARK_OCCURRENCES} is
	 * <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_MARK_CONSTANT_OCCURRENCES = "markConstantOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether local variable occurrences are
	 * marked. Only valid if {@link #EDITOR_MARK_OCCURRENCES} is
	 * <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_MARK_LOCAL_VARIABLE_OCCURRENCES = "markLocalVariableOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether global variable occurrences are
	 * marked. Only valid if {@link #EDITOR_MARK_OCCURRENCES} is
	 * <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.4
	 */
	public static final String EDITOR_MARK_GLOBAL_VARIABLE_OCCURRENCES = "markGlobalVariableOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether exception occurrences are
	 * marked. Only valid if {@link #EDITOR_MARK_OCCURRENCES} is
	 * <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_MARK_EXCEPTION_OCCURRENCES = "markExceptionOccurrences"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether method exit points are marked.
	 * Only valid if {@link #EDITOR_MARK_OCCURRENCES} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_MARK_METHOD_EXIT_POINTS = "markMethodExitPoints"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether targets for of
	 * <code>break</code> and <code>continue</code> statements are marked. Only
	 * valid if {@link #EDITOR_MARK_OCCURRENCES} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.2
	 */
	public static final String EDITOR_MARK_BREAK_CONTINUE_TARGETS = "markBreakContinueTargets"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether method exit points are marked.
	 * Only valid if {@link #EDITOR_MARK_OCCURRENCES} is <code>true</code>.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.1
	 */
	public static final String EDITOR_MARK_IMPLEMENTORS = "markImplementors"; //$NON-NLS-1$

	/**
	 * A named preference prefix for semantic highlighting preferences.
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX = "semanticHighlighting."; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls a semantic highlighting's color.
	 * <p>
	 * Value is of type <code>String</code>. A RGB color value encoded as a
	 * string using class <code>PreferenceConverter</code>
	 * </p>
	 * 
	 * @see org.eclipse.jface.resource.StringConverter
	 * @see org.eclipse.jface.preference.PreferenceConverter
	 * @since 3.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX = ".color"; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls if semantic highlighting has the
	 * text attribute bold.
	 * <p>
	 * Value is of type <code>Boolean</code>: <code>true</code> if bold.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_BOLD_SUFFIX = ".bold"; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls if semantic highlighting has the
	 * text attribute italic.
	 * <p>
	 * Value is of type <code>Boolean</code>: <code>true</code> if italic.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_ITALIC_SUFFIX = ".italic"; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls if semantic highlighting has the
	 * text attribute strikethrough.
	 * <p>
	 * Value is of type <code>Boolean</code>: <code>true</code> if
	 * strikethrough.
	 * </p>
	 * 
	 * @since 3.1
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_STRIKETHROUGH_SUFFIX = ".strikethrough"; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls if semantic highlighting has the
	 * text attribute underline.
	 * <p>
	 * Value is of type <code>Boolean</code>: <code>true</code> if underline.
	 * </p>
	 * 
	 * @since 3.1
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_UNDERLINE_SUFFIX = ".underline"; //$NON-NLS-1$

	/**
	 * A named preference suffix that controls if semantic highlighting is
	 * enabled.
	 * <p>
	 * Value is of type <code>Boolean</code>: <code>true</code> if enabled.
	 * </p>
	 * 
	 * @since 3.0
	 */
	public static final String EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED_SUFFIX = ".enabled"; //$NON-NLS-1$

	/**
	 * A named preference that controls which profile is used by the code
	 * formatter.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 * 
	 */
	public static final String FORMATTER_PROFILE = "formatterProfile"; //$NON-NLS-1$


	/**
	 * A named preference that controls whether all dirty editors are
	 * automatically saved before a refactoring is executed.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String REFACTOR_SAVE_ALL_EDITORS = "RefactoringSavealleditors"; //$NON-NLS-1$

	/**
	 * A named preference that specifies whether children of a PHP file are
	 * shown in the php explorer.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String SHOW_CU_CHILDREN = "explorerCuchildren"; //$NON-NLS-1$

	/**
	 * A named preference that specifies whether children of a php file are
	 * shown in the explorer.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String SHOW_PHP_CHILDREN = "foldersPhpchildren"; //$NON-NLS-1$

	/**
	 * A named preference that controls if templates are formatted when applied.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 */
	public static final String TEMPLATES_USE_CODEFORMATTER = "templateFormat"; //$NON-NLS-1$

	public static final String EDITOR_FOLDING_PHPDOC = "foldPHPDoc"; //$NON-NLS-1$
	public static final String EDITOR_FOLDING_CLASSES = "foldClasses"; //$NON-NLS-1$
	public static final String EDITOR_FOLDING_FUNCTIONS = "foldFunctions"; //$NON-NLS-1$
	//	public static final String EDITOR_FOLDING_INCLUDES = "foldIncludes"; //$NON-NLS-1$

	/**
	 * A named preference that controls whether folding is enabled in the PHP
	 * editor.
	 * <p>
	 * Value is of type <code>Boolean</code>.
	 * </p>
	 * 
	 * @since 3.1
	 * @see IStructuredTextFoldingProvider#FOLDING_ENABLED
	 */
	//	public static final String EDITOR_FOLDING_ENABLED= "editor_folding_enabled"; //$NON-NLS-1$
	// public static final String EDITOR_FOLDING_ENABLED = AbstractStructuredFoldingStrategy.FOLDING_ENABLED;

	/**
	 * A named preference that stores the configured folding provider.
	 * <p>
	 * Value is of type <code>String</code>.
	 * </p>
	 * 
	 * @since 3.1
	 */
	public static final String EDITOR_FOLDING_PROVIDER = "editor_folding_provider"; //$NON-NLS-1$

	/**
	 * The id of the best match hover contributed for extension point
	 * <code>javaEditorTextHovers</code>.
	 */
	public static final String ID_BESTMATCH_HOVER = "org.eclipse.php.ui.editor.hover.BestMatchHover"; //$NON-NLS-1$

	/**
	 * A named preference that defines the key for the hover modifiers.
	 */
	public static final String EDITOR_TEXT_HOVER_MODIFIERS = PHPEplPlugin.PLUGIN_ID
			+ "hoverModifiers"; //$NON-NLS-1$

	/**
	 * A named preference that defines the key for the hover modifier state
	 * masks. The value is only used if the value of
	 * <code>EDITOR_TEXT_HOVER_MODIFIERS</code> cannot be resolved to valid SWT
	 * modifier bits.
	 * 
	 * @see #EDITOR_TEXT_HOVER_MODIFIERS
	 */
	public static final String EDITOR_TEXT_HOVER_MODIFIER_MASKS = PHPEplPlugin.PLUGIN_ID
			+ "hoverModifierMasks"; //$NON-NLS-1$

	/**
	 * some constants for auto-ident Smart Tab
	 */
	public static final String TAB = "tab"; //$NON-NLS-1$
	public static final String FORMATTER_TAB_CHAR = PHPEplPlugin.PLUGIN_ID
			+ ".smart_tab.char"; //$NON-NLS-1$

	public static final String FORMAT_REMOVE_TRAILING_WHITESPACES = "cleanup.remove_trailing_whitespaces"; //$NON-NLS-1$
	public static final String FORMAT_REMOVE_TRAILING_WHITESPACES_ALL = "cleanup.remove_trailing_whitespaces_all"; //$NON-NLS-1$
	public static final String FORMAT_REMOVE_TRAILING_WHITESPACES_IGNORE_EMPTY = "cleanup.remove_trailing_whitespaces_ignore_empty"; //$NON-NLS-1$
	public static final String PREF_OUTLINEMODE = "ChangeOutlineModeAction.selectedMode"; //$NON-NLS-1$

	/**
	 * This setting controls whether to group elements by namespaces in PHP
	 * Explorer
	 */
	public static final String EXPLORER_GROUP_BY_NAMESPACES = "PHPExplorerPart.groupByNamespaces"; //$NON-NLS-1$

	public static IPreferenceStore getPreferenceStore() {
		return PHPEplPlugin.getDefault().getPreferenceStore();
	}

	/**
	 * Initializes the given preference store with the default values.
	 */
	public static void initializeDefaultValues() {
		IPreferenceStore store = getPreferenceStore();
		store.setDefault(SHOW_PHP_CHILDREN, true);
		store.setDefault(SHOW_CU_CHILDREN, true);

		store.setDefault(APPEARANCE_METHOD_RETURNTYPE, false);
		store.setDefault(APPEARANCE_METHOD_TYPEPARAMETERS, true);
		store.setDefault(APPEARANCE_ENABLE_VISIBILITY_SORT_ORDER, false);

		// mark occurrences
		store.setDefault(PreferenceConstants.EDITOR_MARK_OCCURRENCES, true);
		store.setDefault(PreferenceConstants.EDITOR_STICKY_OCCURRENCES, true);
		store.setDefault(PreferenceConstants.EDITOR_MARK_TYPE_OCCURRENCES, true);
		store.setDefault(PreferenceConstants.EDITOR_MARK_METHOD_OCCURRENCES,
				true);
		store.setDefault(PreferenceConstants.EDITOR_MARK_CONSTANT_OCCURRENCES,
				true);
		store.setDefault(PreferenceConstants.EDITOR_MARK_FUNCTION_OCCURRENCES,
				true);
		store.setDefault(
				PreferenceConstants.EDITOR_MARK_LOCAL_VARIABLE_OCCURRENCES,
				true);
		store.setDefault(
				PreferenceConstants.EDITOR_MARK_GLOBAL_VARIABLE_OCCURRENCES,
				true);
		store.setDefault(PreferenceConstants.EDITOR_MARK_EXCEPTION_OCCURRENCES,
				true);
		store.setDefault(PreferenceConstants.EDITOR_MARK_METHOD_EXIT_POINTS,
				true);
		store.setDefault(
				PreferenceConstants.EDITOR_MARK_BREAK_CONTINUE_TARGETS, true);
		store.setDefault(PreferenceConstants.EDITOR_MARK_IMPLEMENTORS, true);

		// RefactoringPreferencePage
		store.setDefault(REFACTOR_SAVE_ALL_EDITORS, false);

		// TemplatePreferencePage
		store.setDefault(TEMPLATES_USE_CODEFORMATTER, true);

		// MembersOrderPreferencePage
		store.setDefault(APPEARANCE_MEMBER_SORT_ORDER, "I,S,T,C,SV,SF,V,F"); //$NON-NLS-1$

		store.setDefault(EDITOR_CORRECTION_INDICATION, true);
		store.setDefault(EDITOR_ANNOTATION_ROLL_OVER, false);

		// Folding options
		// store.setDefault(EDITOR_FOLDING_ENABLED, true);
		store.setDefault(EDITOR_FOLDING_PROVIDER,
				"org.eclipse.php.ui.defaultFoldingProvider"); //$NON-NLS-1$
		store.setDefault(EDITOR_FOLDING_PHPDOC, false);
		store.setDefault(EDITOR_FOLDING_CLASSES, false);
		store.setDefault(EDITOR_FOLDING_FUNCTIONS, false);

		String mod1Name = Action.findModifierString(SWT.MOD1); // SWT.COMMAND on
		// Mac;
		// SWT.CONTROL
		// elsewhere // TODO : Shalom - We might need to remove the text hovers
		store.setDefault(
				EDITOR_TEXT_HOVER_MODIFIERS,
				"org.eclipse.php.ui.editor.hover.BestMatchHover;0;org.eclipse.php.ui.editor.hover.PHPSourceTextHover;" + mod1Name); //$NON-NLS-1$
		store.setDefault(
				EDITOR_TEXT_HOVER_MODIFIER_MASKS,
				"org.eclipse.php.ui.editor.hover.BestMatchHover;0;org.eclipse.php.ui.editor.hover.PHPSourceTextHover;" + SWT.MOD1); //$NON-NLS-1$		

		// default locale (TODO: Shalom - We might need that in
		//		if (store.getString(PHPCoreConstants.WORKSPACE_DEFAULT_LOCALE).equals(
		//				"")) { //$NON-NLS-1$
		//			store.setValue(PHPCoreConstants.WORKSPACE_DEFAULT_LOCALE, Locale
		//					.getDefault().toString());
		//			store.setDefault(PHPCoreConstants.WORKSPACE_LOCALE, Locale
		//					.getDefault().toString());
		//		}

		// save actions
		store.setDefault(FORMAT_REMOVE_TRAILING_WHITESPACES, false);
		store.setDefault(FORMAT_REMOVE_TRAILING_WHITESPACES_ALL, true);
		store.setDefault(FORMAT_REMOVE_TRAILING_WHITESPACES_IGNORE_EMPTY, false);

		store.setDefault(EXPLORER_GROUP_BY_NAMESPACES, false);

		// PHP Semantic Highlighting
		// SemanticHighlightingManager.getInstance().initDefaults(store);

		// do more complicated stuff
		// PHPProjectLayoutPreferencePage.initDefaults(store);
	}

	public static String getEnabledPreferenceKey(String preferenceKey) {
		return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX
				+ preferenceKey
				+ PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED_SUFFIX;
	}

	// Don't instantiate
	private PreferenceConstants() {
	}
}
