package org.vaadin.harry.spring.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReportDetails {

    public static List<ReportDetail> getReportDetails() {
        List<ReportDetail> reportDetails = new ArrayList<>();
        reportDetails.add(new ReportDetail(1, 1, "A very long detail of report 1"));
        reportDetails.add(new ReportDetail(2, 2, "A very long detail of report 1"));
        reportDetails.add(new ReportDetail(3, 3, "A very long detail of report 2"));
        reportDetails.add(new ReportDetail(4, 4, "A very long detail of report 3"));
        reportDetails.add(new ReportDetail(5, 1, "A very long detail of report 1"));
        reportDetails.add(new ReportDetail(6, 2, "A very long detail of report 1"));
        reportDetails.add(new ReportDetail(7, 3, "A very long detail of report 2"));
        reportDetails.add(new ReportDetail(8, 4, "A very long detail of report 3"));
        reportDetails.add(new ReportDetail(9, 1, "A very long detail of report 1"));
        reportDetails.add(new ReportDetail(10, 2, "A very long detail of report 1"));
        reportDetails.add(new ReportDetail(11, 3, "A very long detail of report 2"));
        reportDetails.add(new ReportDetail(12, 4, "A very long detail of report 3"));
        return reportDetails;
    }

    public static Optional<ReportDetail> getReportDetail(int reportId) {
        return getReportDetails().stream().filter(rd -> rd.getId() == reportId).findFirst();
    }
}
