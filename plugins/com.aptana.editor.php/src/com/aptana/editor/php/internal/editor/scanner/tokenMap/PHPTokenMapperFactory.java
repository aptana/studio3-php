package com.aptana.editor.php.internal.editor.scanner.tokenMap;

import org.eclipse.php.internal.core.PHPVersion;

/**
 * A PHP token mapper factory that returns the right {@link IPHPTokenMapper} according to the given {@link PHPVersion}.
 * 
 * @author Shalom Gibly <sgibly@aptana.com>
 */
public class PHPTokenMapperFactory
{
	private static PHP4TokenMapper php4TokenMapper;
	private static PHP5TokenMapper php5TokenMapper;
	private static PHP53TokenMapper php53TokenMapper;

	/**
	 * Returns the {@link IPHPTokenMapper} that match the given {@link PHPVersion}
	 * 
	 * @param phpVersion
	 * @return An {@link IPHPTokenMapper}
	 * @throws IllegalArgumentException
	 *             In case the PHP version is unknown.
	 */
	public static IPHPTokenMapper getMapper(PHPVersion phpVersion)
	{
		switch (phpVersion)
		{
			case PHP4:
				if (php4TokenMapper == null)
				{
					php4TokenMapper = new PHP4TokenMapper();
				}
				return php4TokenMapper;
			case PHP5:
				if (php5TokenMapper == null)
				{
					php5TokenMapper = new PHP5TokenMapper();
				}
				return php5TokenMapper;
			case PHP5_3:
				if (php53TokenMapper == null)
				{
					php53TokenMapper = new PHP53TokenMapper();
				}
				return php53TokenMapper;

		}
		throw new IllegalArgumentException("Unknown PHP version " + phpVersion.getAlias()); //$NON-NLS-1$
	}
}
