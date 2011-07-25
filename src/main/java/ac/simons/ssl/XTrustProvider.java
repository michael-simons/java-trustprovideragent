/**
 * The contents of this file are subject to the "END USER LICENSE AGREEMENT FOR F5
 * Software Development Kit for iControl"; you may not use this file except in
 * compliance with the License. The License is included in the iControl
 * Software Development Kit.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * The Original Code is iControl Code and related documentation
 * distributed by F5.
 *
 * Portions created by F5 are Copyright (C) 1996-2004 F5 Networks
 * Inc. All Rights Reserved.  iControl (TM) is a registered trademark of
 * F5 Networks, Inc.
 *
 * Alternatively, the contents of this file may be used under the terms
 * of the GNU General Public License (the "GPL"), in which case the
 * provisions of GPL are applicable instead of those above.  If you wish
 * to allow use of your version of this file only under the terms of the
 * GPL and not to allow others to use your version of this file under the
 * License, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the GPL.
 * If you do not delete the provisions above, a recipient may use your
 * version of this file under either the License or the GPL.
 */
package ac.simons.ssl;

import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

/**
 * Found here: <a href="http://devcentral.f5.com/weblogs/joe/archive/2005/07/06/1345.aspx">SSL Trust Provider for Java</a>
 */
public final class XTrustProvider extends Provider { 	
	private static final long serialVersionUID = -8153877778027640376L;
	private final static String NAME = "XTrustJSSE"; 
	private final static String INFO =
		"XTrust JSSE Provider (implements trust factory with truststore validation disabled)"; 
	private final static double VERSION = 1.0D; 
	
	public XTrustProvider() { 
		super(NAME, VERSION, INFO); 

		AccessController.doPrivileged(new PrivilegedAction<Object>() { 
			public Object run() { 
				put("TrustManagerFactory." + TrustManagerFactoryImpl.getAlgorithm(), TrustManagerFactoryImpl.class.getName()); 
				return null; 
			} 
		}); 
	} 

	public static void install() { 
		if(Security.getProvider(NAME) == null) { 
			Security.insertProviderAt(new XTrustProvider(), 2); 
			Security.setProperty("ssl.TrustManagerFactory.algorithm", TrustManagerFactoryImpl.getAlgorithm());			
			// Create all-trusting host name verifier
			HostnameVerifier allHostsValid = new HostnameVerifier() {
				public boolean verify(String hostname, SSLSession session) {
					return true;
				}
			};
			// Install the all-trusting host verifier
			HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);			
		} 
	} 

	public final static class TrustManagerFactoryImpl extends TrustManagerFactorySpi { 
		public TrustManagerFactoryImpl() { } 
		public static String getAlgorithm() { return "XTrust509"; } 
		protected void engineInit(KeyStore keystore) throws KeyStoreException { } 
		protected void engineInit(ManagerFactoryParameters mgrparams) throws InvalidAlgorithmParameterException { 
			throw new InvalidAlgorithmParameterException(
					XTrustProvider.NAME + " does not use ManagerFactoryParameters"); 
		} 

		protected TrustManager[] engineGetTrustManagers() { 
			return new TrustManager[] { new X509TrustManager()
			{ 
				public X509Certificate[] getAcceptedIssuers() { return null; } 
				public void checkClientTrusted(X509Certificate[] certs, String authType) { } 
				public void checkServerTrusted(X509Certificate[] certs, String authType) { } 
			}}; 
		}				
	} 
} 