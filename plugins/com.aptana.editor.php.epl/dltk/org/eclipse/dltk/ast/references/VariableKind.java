/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 
 *******************************************************************************/
package org.eclipse.dltk.ast.references;

public interface VariableKind {

	public static final int FIRST_VARIABLE_ID = 0;

	int getId();

	public static class Implementation implements VariableKind {

		private final int id;

		public Implementation(int id) {
			this.id = id;
		}

		public int getId() {
			return id;
		}

		@Override
		public String toString() {
			return "VariableKind-" + id; //$NON-NLS-1$
		}

	}

	/**
	 * @deprecated
	 * @since 2.0
	 */
	@Deprecated
	public class Unknown extends Implementation {

		public static final int ID = FIRST_VARIABLE_ID + 0;

		public Unknown() {
			super(ID);
		}

	}

	/**
	 * @since 2.0
	 */
	@Deprecated
	public class Local extends Implementation {

		public static final int ID = FIRST_VARIABLE_ID + 1;

		public Local() {
			super(ID);
		}

	}

	/**
	 * @since 2.0
	 */
	@Deprecated
	public class Global extends Implementation {

		public static final int ID = FIRST_VARIABLE_ID + 2;

		public Global() {
			super(ID);
		}

	}

	/**
	 * @since 2.0
	 */
	@Deprecated
	public class Instance extends Implementation {

		public static final int ID = FIRST_VARIABLE_ID + 3;

		public Instance() {
			super(ID);
		}

	}

	/**
	 * @since 2.0
	 */
	@Deprecated
	public class Class extends Implementation {

		public static final int ID = FIRST_VARIABLE_ID + 4;

		public Class() {
			super(ID);
		}

	}

	/**
	 * @since 2.0
	 */
	@Deprecated
	public class Mixin extends Implementation {

		public static final int ID = FIRST_VARIABLE_ID + 5;

		public Mixin() {
			super(ID);
		}

	}

	/**
	 * @since 2.0
	 */
	@Deprecated
	public class Argument extends Implementation {

		public static final int ID = FIRST_VARIABLE_ID + 6;

		public Argument() {
			super(ID);
		}

	}

	public static final int LAST_CORE_VARIABLE_ID = FIRST_VARIABLE_ID + 50;

	public static final int LAST_VARIABLE_ID = LAST_CORE_VARIABLE_ID + 50;

	public static final VariableKind UNKNOWN = new Unknown();

	public static final VariableKind LOCAL = new Local();

	public static final VariableKind GLOBAL = new Global();

	public static final VariableKind INSTANCE = new Instance();

	public static final VariableKind CLASS = new Class();

	public static final VariableKind MIXIN = new Mixin();

	public static final VariableKind ARGUMENT = new Argument();

}
