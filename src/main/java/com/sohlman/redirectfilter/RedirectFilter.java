package com.sohlman.redirectfilter;

import com.liferay.portal.kernel.dao.orm.QueryUtil;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.BaseFilter;
import com.liferay.portal.kernel.util.PropsKeys;
import com.liferay.portal.kernel.util.PropsUtil;
import com.liferay.portal.model.VirtualHost;
import com.liferay.portal.service.VirtualHostLocalServiceUtil;
import com.liferay.portal.util.PortalUtil;

import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RedirectFilter extends BaseFilter {
	@Override
	protected Log getLog() {
		return _log;
	}

	@Override
	public boolean isFilterEnabled(
		HttpServletRequest request, HttpServletResponse response) {

		StringBuffer requestURL = request.getRequestURL();

		if (isValidRequestURL(requestURL)) {
			return true;
		}
		else {
			return false;
		}
	}

	protected boolean isValidRequestURL(StringBuffer requestURL) {
		if (requestURL == null) {
			return false;
		}

		String url = requestURL.toString();

		for (String extension : PropsUtil.getArray(PropsKeys.VIRTUAL_HOSTS_IGNORE_EXTENSIONS)) {
			if (url.endsWith(extension)) {
				return false;
			}
		}

		return true;
	}	
	
	@Override
	protected void processFilter(HttpServletRequest request,
			HttpServletResponse response, FilterChain filterChain)
			throws Exception {

		String currentHost = PortalUtil.getHost(request);

		List<VirtualHost> virtualHosts = VirtualHostLocalServiceUtil
				.getVirtualHosts(QueryUtil.ALL_POS, QueryUtil.ALL_POS);

		String redirectHost = null;
		
		for (VirtualHost virtualHost : virtualHosts) {
			String host = virtualHost.getHostname();

			if (currentHost.equals(host)) {
				filterChain.doFilter(request, response);
				return;
			}
			
			if (redirectHost==null) {
				if (currentHost.endsWith(host) || host.endsWith(currentHost)) {
					redirectHost = host;
				}
			}
		}
		if (redirectHost!=null) {
			String protocol = request.isSecure() ? "https://" : "http://";
			String port = request.getServerPort()==80 || request.getServerPort()==443 ? "" : ":" + String.valueOf(request.getServerPort()); 
			String redirectTo = protocol + redirectHost + port;
			try  {
				response.sendRedirect(redirectTo);
			}
			catch(Exception e) {
				_log.error(String.format("currentHost: %s redirectTo:%s URI:%s", currentHost, redirectTo, request.getRequestURI()), e);
			}
		}
		else {
			filterChain.doFilter(request, response);
		}
	}

	private static Log _log = LogFactoryUtil.getLog(RedirectFilter.class);
}
