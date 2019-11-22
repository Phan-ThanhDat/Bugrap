package org.vaadin.harry.spring.views.reportoverview;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.templatemodel.TemplateModel;
import components.ProgressBar;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.bugrap.domain.BugrapRepository;
import org.vaadin.bugrap.domain.entities.Project;
import org.vaadin.bugrap.domain.entities.ProjectVersion;
import org.vaadin.bugrap.domain.entities.Report;


/**
 * A Designer generated component for the report-overview template.
 * <p>
 * Designer will add and remove fields with @Id mappings but
 * does not overwrite or otherwise change this file.
 */
@Tag("report-overview")
@JsModule("./src/views/report-overview/report-overview.js")
@CssImport(value = "./styles/views/report-overview/report-overview.css", themeFor = "vaadin-button")
public class ReportOverview extends PolymerTemplate<ReportOverview.ReportOverviewModel> implements AfterNavigationObserver {

    // connect to Bugrap domain service
    private static BugrapRepository bugrapRepository = new BugrapRepository("/tmp/bugrapdb111;create=true");

    @Id("vaadinComboBox")
    private ComboBox<Project> projectsComboBox;
    @Id("vaadinButtonLogout")
    private Button vaadinButtonLogout;
    @Id("vaadinButtonAccount")
    private Button vaadinButtonAccount;
    TextField searchBar = new TextField();
    @Id("select-project")
    private Select selectVersion;

    @Id("btn-onlyme")
    private Button btnOnlyMe;
    @Id("btn-everyone")
    private Button btnEveryOne;
    @Id("btn-open")
    private Button btnOpen;
    @Id("btn-allkinds")
    private Button btnAllkinds;
    @Id("btn-custom")
    private Button btnCustoms;
    @Id("wrapper-overview")
    private Element wrapperOverview;
    @Id("wrapper-table")
    private Element wrapperTable;

    private boolean isClicked_onlyMe = false;
    private boolean isClicked_Everyone = false;
    private boolean isClicked_report = false;
    @Id("table")
    private Grid<Report> reportTable;
    @Id("wrapper-info")
    private HorizontalLayout wrapperInfo;
    private VerticalLayout footerReport = new VerticalLayout();
    private HorizontalLayout footerTitle = new HorizontalLayout();

    private Select<Report.Priority> selectPriority = new Select<>();
    private Select<Report.Status> status = new Select<>();
    private Select<ProjectVersion> versionSelected = new Select<>();
    private Select<Report.Type> selectType = new Select<>();

    private HorizontalLayout footerContent = new HorizontalLayout();
    private Text author = new Text("");
    private Paragraph reportDetails = new Paragraph();
    private Button btnUpdate = new Button("Update");
    private Button btnRevert = new Button("Revert");
    ConfirmDialog dialogUpdateSucceed = new ConfirmDialog("Update Report",
            "The report is updated!", "OK", this::onOKUpdate);
    private AtomicReference<Set<ProjectVersion>> projectVersions = new AtomicReference(new HashSet<ProjectVersion>());
    private ArrayList<String> listVersions = new ArrayList<String>();
    private List<Report> reportListFilterByProjectAndVersion = new ArrayList<Report>();
    @Id("wrapper-distribution-bar")
    private HorizontalLayout wraperDistributionBar;
    @Id("wrapper-search-bar")
    private HorizontalLayout wrapperSearchBar;

    /**
     * Creates a new ReportOverview.
     */
    public ReportOverview() {
        bugrapRepository.populateWithTestData();
        Icon icon = VaadinIcon.SEARCH.create();
        searchBar.setPrefixComponent(icon);
        searchBar.setClearButtonVisible(true);
        searchBar.setPlaceholder("Searching...");
        searchBar.getStyle().set("width", "80%");
        searchBar.getStyle().set("margin-right", "16px");
        wrapperSearchBar.add(searchBar);
        this.setVisibleOverviewReport();
        // find all projects
        Set<Project> allProjects = this.findAllProjects();

        ArrayList<String> listProjects = new ArrayList<>();
        allProjects.stream().forEach(pr -> {
            listProjects.add(pr.getName());
        });

        //set items to project combo box
        projectsComboBox.setItems(allProjects);
        projectsComboBox.setValue(allProjects.stream().findFirst().get());

        // Titles of report table with grid
        reportTable.addColumn(Report::getPriority).setHeader("PRIORITY");
        reportTable.addColumn(Report::getType).setFlexGrow(0).setWidth("100px").setHeader("TYPE");
        reportTable.addColumn(Report::getSummary).setHeader("SUMMARY");
        reportTable.addColumn(Report::getAssigned).setFlexGrow(0).setWidth("160px").setHeader("ASSIGNED TO");
        reportTable.addColumn(Report::getReportedTimestamp).setHeader("LAST MODIFIED").setWidth("140px");
        reportTable.addColumn(Report::getTimestamp).setHeader("REPORTED").setWidth("140px");

        selectPriority.setItems(Report.Priority.values());
        status.setItems(Report.Status.values());
        selectType.setItems(Report.Type.values());

        // search bar by Priority or type, assigned to
        searchBar.addValueChangeListener(e -> {
            System.out.println(e.getValue());
            if (reportListFilterByProjectAndVersion.size() > 0) {
                List<Report> filterReportList = reportListFilterByProjectAndVersion.stream().filter(r -> (r.getVersion() != null && r.getVersion().getVersion().equalsIgnoreCase(e.getValue()))
                        || (r.getPriority() != null && r.getPriority().toString().equalsIgnoreCase(e.getValue()))
                        || (r.getType() != null && r.getType().toString().equalsIgnoreCase(e.getValue()))
                        || (r.getSummary() != null && r.getSummary().contains(e.getValue()))
                        || (r.getAssigned() != null && r.getAssigned().getName().contains(e.getValue()))).collect(Collectors.toList());
                reportTable.setItems(filterReportList);
            }

        });

        this.setValueForProjectAndVersionWithoutClickEvents(this.projectVersions,
                allProjects,
                listVersions);

        // set grid report table can select muiti rows
        reportTable.getSelectedItems();
        reportTable.setSelectionMode(Grid.SelectionMode.MULTI);
        // listen event listener when triggering clicking
        reportTable.asMultiSelect().addValueChangeListener(this::clickRow);

        // event Click of combo box component to select project
        this.filterReportByProject(allProjects,
                listVersions);

        // event Click of Select component to select project version
        this.filterReportByVersionWhenClickSelectReportList();

//        // UIs
//        vaadinProgressBar.setValue(0.15);
        this.showStatusDistributionBar(0, 0, 0);

        // filter report by clicking Only Me
        this.filterReportByBtnOnlyMe();
        // filter report by clicking EveryOne
        this.filterReportByBtnEveryOne();
        // filter report by clicking Only Me
        this.filterReportByBtnOpen();
        // filter report by clicking All Kinds
        this.filterReportByBtnAllkinds();
        // filter report by clicking Customs
        this.filterReportByBtnCustoms();

        // Set style for footer
        footerTitle.addClassName("wrapper-subject-fields");
        footerTitle.setWidth("100%");
        footerTitle.setDefaultVerticalComponentAlignment(
                FlexComponent.Alignment.CENTER);

        // Set labels for overview report in Footer
        selectPriority.setLabel("Priorrity");
        selectType.setLabel("Type");
        status.setLabel("Label");
        versionSelected.setLabel("Version");
        // Set fields titles in Footer for report details
        btnUpdate.addClassName("custom-margin-top");
        btnRevert.addClassName("custom-margin-top");
        footerTitle.add(selectPriority, selectType, status, versionSelected, btnUpdate, btnRevert);
        //footerTitle.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        footerTitle.setFlexGrow(1, selectPriority);
        footerTitle.setFlexGrow(1, selectType);
        footerTitle.setFlexGrow(1, status);
        footerTitle.setFlexGrow(1, selectVersion);
        footerTitle.setFlexGrow(1, btnUpdate);
        footerTitle.setFlexGrow(1, btnRevert);

        // Add elements to layout (Footer)
        footerReport.add(footerTitle);
        footerContent.add(author);
        footerContent.add(reportDetails);
        footerReport.add(footerContent);
        wrapperInfo.add(footerReport);
    }

    private void showStatusDistributionBar(int first, int second, int third) {
        if (reportListFilterByProjectAndVersion.size() == 0) {
            wraperDistributionBar.removeAll();
            ProgressBar progressBar = new ProgressBar(200, 1000, first, second, third);
            wraperDistributionBar.add(progressBar);
        } else {
            AtomicInteger closed = new AtomicInteger();
            AtomicInteger nonResolved = new AtomicInteger();
            AtomicInteger unassigned = new AtomicInteger();
            reportListFilterByProjectAndVersion.stream().forEach(r -> {
                if (r.getStatus() == null) {
                    unassigned.addAndGet(1);
                } else if (r.getStatus().toString().toLowerCase().equals("won't fix") ||
                        r.getStatus().toString().toLowerCase().equals("duplicate")) {
                    nonResolved.addAndGet(1);
                } else {
                    closed.addAndGet(1);
                }
            });
            int firstClosed = closed.intValue();
            int secondNonResolved = nonResolved.intValue();
            int thirdUnassigned = unassigned.intValue();
            wraperDistributionBar.removeAll();
            ProgressBar progressBar = new ProgressBar(200, 1000, firstClosed, secondNonResolved, thirdUnassigned);
            wraperDistributionBar.add(progressBar);
        }

    }

    private void filterReportByBtnCustoms() {
    }

    private void filterReportByBtnAllkinds() {
        btnAllkinds.addClickListener(this::showButtonClicked_allkindsBtn);
    }

    private void showButtonClicked_allkindsBtn(ClickEvent<Button> buttonClickEvent) {
        if (reportListFilterByProjectAndVersion.size() > 0) {
            reportTable.setItems(reportListFilterByProjectAndVersion);
            btnAllkinds.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnOpen.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnEveryOne.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnOnlyMe.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            this.setVisibleOverviewReport();
        }
    }

    private void showButtonClickedMessage_everyone(ClickEvent<Button> buttonClickEvent) {
        isClicked_onlyMe = false;
        isClicked_Everyone = true;

        if (reportListFilterByProjectAndVersion.size() > 0) {
            reportTable.setItems(reportListFilterByProjectAndVersion);
            this.setVisibleOverviewReport();
            if (isClicked_Everyone) {
                btnOpen.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                btnAllkinds.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                btnEveryOne.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                btnOnlyMe.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }
        }
    }

    private void filterReportByBtnEveryOne() {
        btnEveryOne.addClickListener(this::showButtonClickedMessage_everyone);
    }

    private void showButtonClicked_openBtn(ClickEvent<Button> buttonClickEvent) {
        if (reportListFilterByProjectAndVersion.size() > 0) {
            btnOpen.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnAllkinds.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnEveryOne.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            btnOnlyMe.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            List<Report> reportFilterByOpenStatus = reportListFilterByProjectAndVersion
                    .stream()
                    .filter(r -> r.getStatus() == null)
                    .collect(Collectors.toList());

            reportTable.setItems(reportFilterByOpenStatus);
            this.setVisibleOverviewReport();

        }
    }

    private void filterReportByBtnOpen() {
        btnOpen.addClickListener(this::showButtonClicked_openBtn);
    }

    private void showButtonClickedMessage_onlyMe(ClickEvent<Button> buttonClickEvent) {
        isClicked_onlyMe = true;
        isClicked_Everyone = false;
        if (reportListFilterByProjectAndVersion.size() > 0) {
            List<Report> reportOnlyMe = reportListFilterByProjectAndVersion
                    .stream()
                    .filter(report -> report != null && report.getAssigned() != null && report.getAssigned().getName().toLowerCase().equals("developer"))
                    .collect(Collectors.toList());
            reportTable.setItems(reportOnlyMe);
            this.setVisibleOverviewReport();
            if (isClicked_onlyMe) {
                btnOpen.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                btnAllkinds.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
                btnOnlyMe.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
                btnEveryOne.removeThemeVariants(ButtonVariant.LUMO_PRIMARY);
            }
        }
    }

    private void filterReportByBtnOnlyMe() {
        btnOnlyMe.addClickListener(this::showButtonClickedMessage_onlyMe);
    }

    private Set<Project> findAllProjects() {
        return bugrapRepository.findProjects();
    }

    private void setValueToVersionProject (Set<Project> allProjects) {
        Project project = allProjects.stream().filter(p -> {
            return p.getName().toLowerCase().equals(projectsComboBox.getValue().toString().toLowerCase());
        }).findFirst().get();

        if (project != null) {
            this.projectVersions.set(bugrapRepository.findProjectVersions(project));

            this.projectVersions.get().forEach(version -> {
                listVersions.add(version.getVersion());
            });

            selectVersion.setItems(listVersions);
            selectVersion.setValue(listVersions.get(0));
        }
    }

    private void setValueForProjectAndVersionWithoutClickEvents(AtomicReference<Set<ProjectVersion>> projectVersions,
                                                                Set<Project> allProjects,
                                                                ArrayList<String> listVersions) {

        this.setValueToVersionProject(allProjects);
        String selectVersionValue = String.valueOf(selectVersion.getValue());
        this.filterReportByProjectAndVersion(projectsComboBox.getValue().toString(), selectVersionValue);
        this.setVisibleOverviewReport();
    }

    private void filterReportByProject(Set<Project> allProjects,
                                       ArrayList<String> listVersions) {
        projectsComboBox.addValueChangeListener(
                e -> {
                    this.setValueToVersionProject(allProjects);
                    this.filterReportByProjectAndVersion(e.getValue().toString(), listVersions.get(0));
                    this.showStatusDistributionBar(0, 0, 0);
                });
    }

    private  List<org.vaadin.bugrap.domain.entities.Report>  checkReportListFilterByProject (String projectSelected,
                                                 String version) {
        Set<org.vaadin.bugrap.domain.entities.Report> reports
                = this.listReports(null, null, bugrapRepository);

        return  reports.stream()
                .filter(rl -> rl.getProject() != null)
                .filter(r -> projectSelected.equalsIgnoreCase(r.getProject().getName()))
                .collect(Collectors.toList());
    }

    private void setValueForReportTable () {
        reportTable.getDataProvider().refreshAll();
        reportTable.setItems(reportListFilterByProjectAndVersion);
        this.setVisibleOverviewReport();
    }
    private void filterReportByProjectAndVersion(String projectSelected, String version) {
//        Set<org.vaadin.bugrap.domain.entities.Report> reports
//                = this.listReports(null, null, bugrapRepository);
//
        List<org.vaadin.bugrap.domain.entities.Report> reportListFilterByProject =
                this.checkReportListFilterByProject(projectSelected, version);
//            // version
        if (!reportListFilterByProject.isEmpty()) {
            reportListFilterByProjectAndVersion = reportListFilterByProject
                    .stream()
                    .filter(rl ->
                            rl.getVersion() != null)
                    .filter(listReport -> listReport.getVersion().getVersion().equalsIgnoreCase(version))
                    .collect(Collectors.toList());
            this.setValueForReportTable();
        }
    }

    private void filterReportByVersionWhenClickSelectReportList() {
        selectVersion.addValueChangeListener(version -> {
            String projectSelected = projectsComboBox.getValue().toString();
            String projectVersionSelected = String.valueOf(version.getValue());
            this.checkReportListFilterByProject(projectSelected, version.toString());

            List<org.vaadin.bugrap.domain.entities.Report> reportListFilterByProject =
                    this.checkReportListFilterByProject(projectSelected, version.toString());
//            // version
            if (!reportListFilterByProject.isEmpty()) {
                reportListFilterByProjectAndVersion = reportListFilterByProject
                        .stream()
                        .filter(rl -> rl.getVersion() != null
                                && !StringUtils.isEmpty(rl.getVersion().getVersion())
                                && rl.getVersion().getVersion().toLowerCase().equals(projectVersionSelected.toLowerCase()))
                        .collect(Collectors.toList());
                this.setValueForReportTable();
            }
            this.showStatusDistributionBar(0, 0, 0);
        });
    }

    public Set<org.vaadin.bugrap.domain.entities.Report> listReports(Project project, ProjectVersion projectVersion, BugrapRepository bugrapRepository) {
        BugrapRepository.ReportsQuery query = new BugrapRepository.ReportsQuery();
        query.project = project;
        query.projectVersion = projectVersion;
        Set<org.vaadin.bugrap.domain.entities.Report> reports = bugrapRepository.findReports(query);
        return reports;
    }

    private void setVisibleOverviewReport() {
        if (reportTable.getSelectedItems().isEmpty()) {
            wrapperOverview.setVisible(false);
        } else {
            wrapperOverview.setVisible(true);
        }
    }

    private Report reportUpdated = new Report();
    private List<Report> reportsUpdated = new ArrayList<Report>();

    // Method binding data to 4 fields Priority, Status, Type and Version
    private void bindingDataForFileds(Binder<Report> binderReport) {
        selectPriority.addValueChangeListener(e -> {
            binderReport.forField(selectPriority)
                    .bind(Report::getPriority, Report::setPriority);
        });
        selectType.addValueChangeListener(e -> {
            binderReport.forField(selectType)
                    .bind(Report::getType, Report::setType);
        });
        status.addValueChangeListener(e -> {
            binderReport.forField(status)
                    .bind(Report::getStatus, Report::setStatus);
        });
        versionSelected.addValueChangeListener(e -> {
            binderReport.forField(versionSelected)
                    .bind(Report::getVersion, Report::setVersion);
        });

    }

    // Revert button (function) for clicking one row
    private void clickRevertBtnForOneRow(Report oldReport) {
        btnRevert.addClickListener(e -> {
            selectPriority.setValue(oldReport.getPriority());
            selectType.setValue(oldReport.getType());
            status.setValue(oldReport.getStatus());
            versionSelected.setValue(oldReport.getVersion());
        });
    }

    // Revert button (function) for clicking one row
    private void clickRevertBtnForMultiRows(Report report) {
        btnRevert.addClickListener(e -> {
            selectPriority.setValue(report.getPriority());
            selectType.setValue(report.getType());
            this.status.setValue(report.getStatus());
            versionSelected.setValue(report.getVersion());
        });
    }

    private void clickUpdateBtnForOneRow(Binder<Report> binderReport, String currentVersion) {
        btnUpdate.addClickListener(e -> {
            try {
                binderReport.writeBean(reportUpdated);
                reportUpdated = bugrapRepository.save(reportUpdated);
                this.filterReportByProjectAndVersion(reportUpdated.getProject().toString(), currentVersion);
                dialogUpdateSucceed.open();

            } catch (ValidationException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void implementSelectOneRow(Set<Report> getReport) {
        reportUpdated = getReport.iterator().next();
        Report oldReport = reportUpdated;
        String currentVersion = reportUpdated.getVersion().getVersion();

        versionSelected.setItems(this.projectVersions.get());

        selectPriority.setValue(reportUpdated.getPriority());
        selectType.setValue(reportUpdated.getType());
        status.setValue(reportUpdated.getStatus());
        versionSelected.setValue(reportUpdated.getVersion());

        //render report detail in overview report in footer with author and description of the selected report
        author.setText(reportUpdated.getAuthor() != null ? reportUpdated.getAuthor().getName() : "Unknown");
        reportDetails.setText(reportUpdated.getDescription() != null ? reportUpdated.getDescription() : "No description");

        //binding data to 4 fields Priority, Status, Type and Version
        Binder<Report> binderReport = new Binder<Report>();
        this.bindingDataForFileds(binderReport);

        // trigger Revert Button
        this.clickRevertBtnForOneRow(oldReport);

        // trigger Update Button
        this.clickUpdateBtnForOneRow(binderReport, currentVersion);
    }

    private void checkFieldsAreDiff (boolean isAllPrioritySame , boolean isAllStatusSame, boolean isAllTypeSame ) {
        for (int i = 0; i < reportsUpdated.size() - 1; i++) {
            for (int k = i + 1; k < reportsUpdated.size(); k++) {

                // Check Priority, Status and Type whether diff or not
                //Priority
                if (reportsUpdated.get(i).getPriority() != null && reportsUpdated.get(k).getPriority() != null) {
                    isAllPrioritySame = reportsUpdated.get(i).getPriority().toString()
                            .equalsIgnoreCase(reportsUpdated.get(k).getPriority().toString());
                }

                // Status
                if (reportsUpdated.get(i).getStatus() != null && reportsUpdated.get(k).getStatus() != null) {
                    isAllStatusSame = reportsUpdated.get(i).getStatus().toString()
                            .equalsIgnoreCase(reportsUpdated.get(k).getStatus().toString());
                }

                // Type
                if (reportsUpdated.get(i).getType() != null && reportsUpdated.get(k).getType() != null) {
                    isAllTypeSame = reportsUpdated.get(i).getType().toString()
                            .equalsIgnoreCase(reportsUpdated.get(k).getType().toString());
                }
            }
        }
    }

    private void checkFieldsHasSameValueAndGiveValues (
            boolean isAllPrioritySame,
            boolean isAllStatusSame,
            boolean isAllTypeSame,
            boolean isDiffFieldsValue,
            Report firstReport) {

        if (isAllPrioritySame) {
            selectPriority.setValue(firstReport.getPriority());
        } else {
            selectPriority.setValue(null);
            isDiffFieldsValue =  true;
        }

        if (isAllStatusSame) {
            status.setValue(firstReport.getStatus());
        } else {
            status.setValue(null);
            isDiffFieldsValue =  true;
        }

        if (isAllTypeSame) {
            selectType.setValue(firstReport.getType());
        } else {
            selectType.setValue(null);
            isDiffFieldsValue =  true;
        }

        // check buttons Revert and Update should be hidden or not
        if (isDiffFieldsValue) {
            btnUpdate.setVisible(false);
        }
        else {
            btnUpdate.setVisible(true);

            // handle Revert button (function) for multi rows
            this.clickRevertBtnForMultiRows(firstReport);
        }
    }

    private void checkAuthorAndDescritionAreDiff (boolean isSameAuthor,
                                                  boolean isSameDescription,
                                                  Report firstReport) {
        for (int i = 0; i < reportsUpdated.size() - 1; i++) {
            for (int k = i + 1; k < reportsUpdated.size(); k++) {

                // Author
                if (reportsUpdated.get(i).getAuthor() != null && reportsUpdated.get(k).getAuthor() != null) {
                    isSameAuthor = reportsUpdated.get(i).getAuthor().toString()
                            .equalsIgnoreCase(reportsUpdated.get(k).getAuthor().toString());
                }

                // Description
                if (reportsUpdated.get(i).getDescription() != null && reportsUpdated.get(k).getDescription() != null) {
                    isSameDescription = reportsUpdated.get(i).getDescription().toString()
                            .equalsIgnoreCase(reportsUpdated.get(k).getDescription().toString());
                }

                //render report detail in overview report in footer with author and description of the selected report
                author.setText( firstReport.getAuthor() != null && isSameAuthor
                        ? firstReport.getAuthor().getName().toString() : "Unknown");
                reportDetails.setText(firstReport.getDescription() != null && isSameDescription
                        ? firstReport.getDescription().toString() : "No description");
            }
        }
    }

    private void implementSelectMultiRows(Set<Report> getReport) {
        reportsUpdated = (List<Report>) getReport.stream().collect(Collectors.toList());
        Report firstReport = reportsUpdated.stream().findFirst().get();
        Report oldReport = firstReport;
        String currentVersion = firstReport.getVersion().getVersion();

        versionSelected.setItems(this.projectVersions.get());
        boolean isAllPrioritySame = true, isAllStatusSame = true, isAllTypeSame = true;

        this.checkFieldsAreDiff(isAllPrioritySame, isAllStatusSame, isAllTypeSame);

        // set value to version field
        versionSelected.setValue(firstReport.getVersion());

        // Check if field Priority  has all elements are same, set value to the field,
        // same with 2 others fields
        boolean isDiffFieldsValue = false;
        this.checkFieldsHasSameValueAndGiveValues(isAllPrioritySame, isAllStatusSame, isAllTypeSame, isDiffFieldsValue, firstReport);


        // check authors and descriptions are same of all reports
        boolean isSameAuthor = false, isSameDescription = false;
        this.checkAuthorAndDescritionAreDiff(isSameAuthor, isSameDescription, firstReport);

//
        //binding data to 4 fields Priority, Status, Type and Version
        Binder<Report> binderReport = new Binder<Report>();
        this.bindingDataForFileds(binderReport);

        // trigger Revert Button
        this.clickRevertBtnForOneRow(oldReport);
    }

    private void clickRow(AbstractField.ComponentValueChangeEvent<Grid<Report>, Set<Report>> gridSetComponentValueChangeEvent) {
        this.setVisibleOverviewReport();

        Set<Report> getReport = gridSetComponentValueChangeEvent.getValue();

        // if selected 1 row
        if (gridSetComponentValueChangeEvent.getValue().size() == 1) {
            this.implementSelectOneRow(getReport);
        } else if (gridSetComponentValueChangeEvent.getValue().size() > 1) {
            this.implementSelectMultiRows(getReport);
        }
    }

    private void onOKUpdate(ConfirmDialog.ConfirmEvent confirmEvent) {
        System.out.println(confirmEvent);
        reportTable.deselectAll();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent afterNavigationEvent) {
    }

    /**
     * This model binds properties between ReportOverview and report-overview
     */
    public interface ReportOverviewModel extends TemplateModel {
    }

}
