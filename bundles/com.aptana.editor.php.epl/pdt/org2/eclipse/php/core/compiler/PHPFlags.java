package org2.eclipse.php.core.compiler;

public class PHPFlags implements IPHPModifiers
{
	/**
	 * Returns whether the given integer includes the <code>default</code> modifier. That usually means that the element
	 * has no 'public', 'protected' or 'private' modifiers at all.
	 * 
	 * @param flags
	 *            the flags
	 * @return <code>true</code> if the <code>default</code> modifier is included
	 */
	public static boolean isDefault(int flags)
	{
		return !isPrivate(flags) && !isProtected(flags) && !isPublic(flags);
	}

	/**
	 * Returns whether the given integer includes the <code>namespace</code> modifier.
	 * 
	 * @param flags
	 *            the flags
	 * @return <code>true</code> if the <code>namespace</code> modifier is included
	 */
	public static boolean isNamespace(int flags)
	{
		return (flags & AccNameSpace) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>constant</code> modifier.
	 * 
	 * @param flags
	 *            the flags
	 * @return <code>true</code> if the <code>constant</code> modifier is included
	 */
	public static boolean isConstant(int flags)
	{
		return (flags & AccConstant) != 0;
	}
	
	/**
	 * Returns whether the given integer includes a <code>named-constant</code>(e.g. Define) modifier.
	 * 
	 * @param flags
	 *            the flags
	 * @return <code>true</code> if a <code>named constant</code> modifier is included
	 */
	public static boolean isNamedConstant(int flags)
	{
		return (flags & NAMED_CONSTANT) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>class</code> modifier.
	 * 
	 * @param flags
	 *            the flags
	 * @return <code>true</code> if the <code>class</code> modifier is included
	 */
	public static boolean isClass(int flags)
	{
		return !isNamespace(flags) && !isInterface(flags);
	}

	/**
	 * Returns whether the given integer includes the <code>private</code> modifier.
	 * 
	 * @param flags
	 *            the flags
	 * @return <code>true</code> if the <code>private</code> modifier is included
	 */
	public static boolean isPrivate(int flags)
	{
		return (flags & AccPrivate) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>protected</code> modifier.
	 * 
	 * @param flags
	 *            the flags
	 * @return <code>true</code> if the <code>protected</code> modifier is included
	 */
	public static boolean isProtected(int flags)
	{
		return (flags & AccProtected) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>public</code> modifier.
	 * 
	 * @param flags
	 *            the flags
	 * @return <code>true</code> if the <code>public</code> modifier is included
	 */
	public static boolean isPublic(int flags)
	{
		return (flags & AccPublic) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>static</code> modifier.
	 * 
	 * @param flags
	 *            the flags
	 * @return <code>true</code> if the <code>static</code> modifier is included
	 */
	public static boolean isStatic(int flags)
	{
		return (flags & AccStatic) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>final</code> modifier.
	 * 
	 * @param flags
	 *            the flags
	 * @return <code>true</code> if the <code>final</code> modifier is included
	 */
	public static boolean isFinal(int flags)
	{
		return (flags & AccFinal) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>abstract</code> modifier.
	 * 
	 * @param flags
	 *            the flags
	 * @return <code>true</code> if the <code>abstract</code> modifier is included
	 */
	public static boolean isAbstract(int flags)
	{
		return (flags & AccAbstract) != 0;
	}

	/**
	 * Returns whether the given integer includes the <code>interface</code> modifier.
	 * 
	 * @param flags
	 *            the flags
	 * @return <code>true</code> if the <code>interface</code> modifier is included
	 */
	public static boolean isInterface(int flags)
	{
		return (flags & AccInterface) != 0;
	}

	/**
	 * Returns whether the given integer includes the indication that the element is synthetic.
	 * 
	 * @param flags
	 *            the flags
	 * @return <code>true</code> if the element is marked synthetic
	 * @since 2.0
	 */
	public static boolean isSynthetic(int flags)
	{
		return (flags & AccSynthetic) != 0;
	}

	/**
	 * Returns a string representation of the given flag.
	 * 
	 * @param mod
	 * @return
	 */
	public static String toString(int mod)
	{
		StringBuffer sb = new StringBuffer();

		if ((mod & AccPublic) != 0)
		{
			sb.append("public "); //$NON-NLS-1$
		}
		if ((mod & AccProtected) != 0)
		{
			sb.append("protected "); //$NON-NLS-1$
		}
		if ((mod & AccPrivate) != 0)
		{
			sb.append("private "); //$NON-NLS-1$
		}

		// Canonical order
		if ((mod & AccAbstract) != 0)
		{
			sb.append("abstract "); //$NON-NLS-1$
		}
		if ((mod & AccStatic) != 0)
		{
			sb.append("static "); //$NON-NLS-1$
		}
		if ((mod & AccFinal) != 0)
		{
			sb.append("final "); //$NON-NLS-1$
		}

		int len;
		if ((len = sb.length()) > 0)
		{ /* trim trailing space */
			return sb.toString().substring(0, len - 1);
		}
		return ""; //$NON-NLS-1$
	}
}
