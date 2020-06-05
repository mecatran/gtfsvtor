package com.mecatran.gtfsvtor.reporting;

import com.mecatran.gtfsvtor.loader.DataObjectSourceInfo;

public interface ReportSink {

	public void report(ReportIssue issue);

	/**
	 * @param issue Issue having a list of source info ref
	 * @param infoList The list of source infos for this ref, if available. The
	 *        list can contain null value if not available, and should be in the
	 *        same order as the issue references. This method is only for
	 *        optimization purposes: if some source info is directly available
	 *        at the time we report an issue (usually in streaming validator,
	 *        were the source context is still available), it's better to store
	 *        it right-away. With some luck, this may save some source
	 *        rescanning later-on, if the data has not too many issues.
	 */
	public void report(ReportIssue issue, DataObjectSourceInfo... infoList);
}
