/**
 * This file Copyright (c) 2005-2010 Aptana, Inc. This program is
 * dual-licensed under both the Aptana Public License and the GNU General
 * Public license. You may elect to use one or the other of these licenses.
 * 
 * This program is distributed in the hope that it will be useful, but
 * AS-IS and WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, TITLE, or
 * NONINFRINGEMENT. Redistribution, except as permitted by whichever of
 * the GPL or APL you select, is prohibited.
 *
 * 1. For the GPL license (GPL), you can redistribute and/or modify this
 * program under the terms of the GNU General Public License,
 * Version 3, as published by the Free Software Foundation.  You should
 * have received a copy of the GNU General Public License, Version 3 along
 * with this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Aptana provides a special exception to allow redistribution of this file
 * with certain other free and open source software ("FOSS") code and certain additional terms
 * pursuant to Section 7 of the GPL. You may view the exception and these
 * terms on the web at http://www.aptana.com/legal/gpl/.
 * 
 * 2. For the Aptana Public License (APL), this program and the
 * accompanying materials are made available under the terms of the APL
 * v1.0 which accompanies this distribution, and is available at
 * http://www.aptana.com/legal/apl/.
 * 
 * You may view the GPL, Aptana's exception and additional terms, and the
 * APL in the file titled license.html at the root of the corresponding
 * plugin containing this source file.
 * 
 * Any modifications to this file must keep this entire header intact.
 */

package com.aptana.filesystem.ftp.tests;

import java.io.IOException;
import java.text.ParseException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;

import com.aptana.filesystem.ftp.FTPConnectionPoint;
import com.aptana.filesystem.ftp.wrappers.FTPProxiedConnectionFileManager;
import com.aptana.filesystem.ftp.wrappers.FTPProxiedConnectionPoint;
import com.aptana.ide.core.io.ConnectionContext;
import com.aptana.ide.core.io.CoreIOPlugin;
import com.enterprisedt.net.ftp.FTPException;

/**
 * @author Max Stepanov
 */
public class FTPProxiedConnectionTest extends FTPCommonConnectionTest
{
	@Override
	public FTPConnectionPoint getConnectionPoint()
	{
		return new FTPProxiedConnectionPoint();
	}

	public final void testNavigateRemoteSystem() throws CoreException, IOException
	{
		FTPProxiedConnectionPoint ftpcp = (FTPProxiedConnectionPoint) cp;
		IFileStore fs = cp.getRoot().getFileStore(testPath.append("/test_folder")); //$NON-NLS-1$

		cp.disconnect(null);
		fs.mkdir(EFS.SHALLOW, null);
		assertTrue(fs.fetchInfo().exists());

		cp.disconnect(null);
		ftpcp.forceStreamException(true);
		try
		{
			fs.getChild("file.txt").openOutputStream(EFS.NONE, null); //$NON-NLS-1$
			fail();
		}
		catch (CoreException e)
		{
			assertFalse(fs.getChild("file.txt").fetchInfo().exists()); //$NON-NLS-1$			
		}

		ftpcp.forceStreamException(true);
		try
		{
			fs.getChild("file2.txt").openOutputStream(EFS.NONE, null); //$NON-NLS-1$
			fail();
		}
		catch (CoreException e)
		{
			assertFalse(fs.getChild("file.txt").fetchInfo().exists()); //$NON-NLS-1$			
		}
	}

	/**
	 * Unhappy I have to create wrappers which violate public /protected, but it's the only way I can test a huge chunk
	 * of code without resorting to refactoring this
	 * 
	 * @throws CoreException
	 * @throws ParseException
	 * @throws IOException
	 * @throws FTPException
	 */
	public final void testDetectTimezone() throws CoreException, FTPException, IOException, ParseException
	{
		FTPProxiedConnectionPoint ftpcp = (FTPProxiedConnectionPoint) cp;

		ConnectionContext context = CoreIOPlugin.getConnectionContext(cp);
		Boolean detect = context.getBoolean(ConnectionContext.DETECT_TIMEZONE);
		context.setBoolean(ConnectionContext.DETECT_TIMEZONE, true);

		FTPProxiedConnectionFileManager cfp = (FTPProxiedConnectionFileManager) ftpcp.getConnectionFileManager();
		cfp.connect(null);
		cfp.determineServerTimeZoneShift(context, null, testPath, testPath);

		context.setBoolean(ConnectionContext.DETECT_TIMEZONE, detect);
	}

}
