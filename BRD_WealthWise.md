```latex
\documentclass[12pt,a4paper]{article}

% ── Packages ────────────────────────────────────────────────────────────────
\usepackage[margin=2.5cm]{geometry}
\usepackage{array}
\usepackage{booktabs}
\usepackage{longtable}
\usepackage{xcolor}
\usepackage{colortbl}
\usepackage{tabularx}
\usepackage{titlesec}
\usepackage{fancyhdr}
\usepackage{graphicx}
\usepackage{hyperref}
\usepackage{enumitem}
\usepackage{parskip}
\usepackage{multirow}
\usepackage{listings}
\usepackage{lmodern}
\usepackage[T1]{fontenc}
\usepackage[utf8]{inputenc}

% ── Colors ──────────────────────────────────────────────────────────────────
\definecolor{primaryblue}{RGB}{30, 90, 200}
\definecolor{lightblue}{RGB}{220, 235, 255}
\definecolor{darkgray}{RGB}{50, 50, 50}
\definecolor{lightgray}{RGB}{245, 245, 245}
\definecolor{tableheader}{RGB}{30, 90, 200}
\definecolor{tablerow}{RGB}{240, 245, 255}

% ── Hyperref Setup ───────────────────────────────────────────────────────────
\hypersetup{
    colorlinks=true,
    linkcolor=primaryblue,
    urlcolor=primaryblue,
    pdftitle={BRD - WealthWise Financial Tracker},
    pdfauthor={WealthWise Team},
}

% ── Section Styling ──────────────────────────────────────────────────────────
\titleformat{\section}
  {\color{primaryblue}\large\bfseries}
  {\thesection.}{0.5em}{}[\titlerule]

\titleformat{\subsection}
  {\color{darkgray}\normalsize\bfseries}
  {\thesubsection.}{0.5em}{}

% ── Header / Footer ──────────────────────────────────────────────────────────
\pagestyle{fancy}
\fancyhf{}
\rhead{\textcolor{primaryblue}{\textbf{WealthWise BRD}}}
\lhead{\textcolor{darkgray}{Business Requirements Document}}
\rfoot{\textcolor{darkgray}{\thepage}}
\lfoot{\textcolor{darkgray}{Confidential — v1.0}}
\renewcommand{\headrulewidth}{0.4pt}
\renewcommand{\footrulewidth}{0.4pt}

% ── Table helpers ────────────────────────────────────────────────────────────
\newcolumntype{L}[1]{>{\raggedright\arraybackslash}p{#1}}
\newcolumntype{C}[1]{>{\centering\arraybackslash}p{#1}}

\newcommand{\theader}[1]{%
  \rowcolor{tableheader}\textcolor{white}{\textbf{#1}}%
}

% ── Listings (for architecture block) ───────────────────────────────────────
\lstset{
  basicstyle=\ttfamily\small,
  backgroundcolor=\color{lightgray},
  frame=single,
  framesep=4pt,
  breaklines=true,
}

% ════════════════════════════════════════════════════════════════════════════
\begin{document}

% ── Title Page ───────────────────────────────────────────────────────────────
\begin{titlepage}
  \centering
  \vspace*{2cm}
  {\color{primaryblue}\rule{\linewidth}{2pt}}\\[0.4cm]
  {\Huge\bfseries\color{primaryblue} WealthWise}\\[0.2cm]
  {\Large\color{darkgray} Personal Financial Tracker}\\[0.1cm]
  {\color{primaryblue}\rule{\linewidth}{2pt}}\\[1cm]

  {\LARGE\bfseries Business Requirements Document}\\[2cm]

  \begin{tabular}{L{5cm} L{7cm}}
    \rowcolor{lightblue}
    \textbf{Document Version} & 1.0 \\
    \textbf{Date}             & March 9, 2026 \\
    \rowcolor{lightblue}
    \textbf{Status}           & Draft \\
    \textbf{Project Name}     & WealthWise \\
    \rowcolor{lightblue}
    \textbf{Document Type}    & Business Requirements Document \\
  \end{tabular}

  \vfill
  {\small\color{darkgray} Confidential — Internal Use Only}
\end{titlepage}

% ── Table of Contents ────────────────────────────────────────────────────────
\tableofcontents
\newpage

% ════════════════════════════════════════════════════════════════════════════
\section{Executive Summary}

WealthWise is a personal financial tracking web application that enables individuals to
monitor income, expenses, savings, and investments in one centralized platform.
The application provides actionable insights, goal tracking, financial forecasting, and
automated reporting to help users make informed financial decisions and work toward
long-term financial health.

% ════════════════════════════════════════════════════════════════════════════
\section{Business Objectives}

\begin{tabularx}{\textwidth}{C{2cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{ID}} & \textcolor{white}{\textbf{Objective}} \\
  \midrule
  BO-01 & Provide a centralized platform to record and categorize all financial transactions. \\
  \rowcolor{tablerow}
  BO-02 & Help users set and track financial goals (e.g., emergency fund, vacation savings). \\
  BO-03 & Deliver predictive financial forecasts based on historical spending patterns. \\
  \rowcolor{tablerow}
  BO-04 & Generate automated alerts for budget overruns and goal milestones. \\
  BO-05 & Enable downloadable monthly financial reports in PDF and CSV formats. \\
  \rowcolor{tablerow}
  BO-06 & Ensure data privacy and security through user authentication and authorization. \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{Scope}

\subsection{In Scope}
\begin{itemize}[leftmargin=1.5em]
  \item User registration and authentication (JWT-based)
  \item Transaction management (create, read, update, delete)
  \item Financial goals management with deposit tracking
  \item Dashboard with summary statistics and visual charts
  \item Financial forecasting based on historical data
  \item Financial insights and recommendations
  \item Notification system for automated alerts
  \item Monthly report generation (PDF \& CSV export)
\end{itemize}

\subsection{Out of Scope}
\begin{itemize}[leftmargin=1.5em]
  \item Mobile application (iOS / Android)
  \item Bank account integration / open banking (e.g., Plaid)
  \item Multi-currency support
  \item Shared or family financial accounts
  \item Tax filing or tax advisory features
  \item Investment portfolio management
\end{itemize}

% ════════════════════════════════════════════════════════════════════════════
\section{Stakeholders}

\begin{tabularx}{\textwidth}{L{3cm} L{3.5cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Stakeholder}} &
  \textcolor{white}{\textbf{Role}} &
  \textcolor{white}{\textbf{Interest}} \\
  \midrule
  End User         & Primary user     & Track personal finances, achieve savings goals \\
  \rowcolor{tablerow}
  Product Owner    & Decision maker   & Prioritize features, define acceptance criteria \\
  Development Team & Builders         & Implement and maintain the system \\
  \rowcolor{tablerow}
  Security Team    & Reviewers        & Ensure data privacy and authentication standards \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{Functional Requirements}

\subsection{Authentication \& User Management}

\begin{tabularx}{\textwidth}{C{1.8cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{ID}} & \textcolor{white}{\textbf{Requirement}} \\
  \midrule
  FR-01 & The system shall allow new users to register with a username, full name, email, and password. \\
  \rowcolor{tablerow}
  FR-02 & The system shall authenticate users and return a JWT token valid for 24 hours. \\
  FR-03 & The system shall reject API requests without a valid JWT token. \\
  \rowcolor{tablerow}
  FR-04 & Passwords shall be stored in hashed form and never returned in API responses. \\
  \bottomrule
\end{tabularx}

\subsection{Transaction Management}

\begin{tabularx}{\textwidth}{C{1.8cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{ID}} & \textcolor{white}{\textbf{Requirement}} \\
  \midrule
  FR-05 & The system shall support four transaction types: \textbf{Income}, \textbf{Expense}, \textbf{Saving}, and \textbf{Investment}. \\
  \rowcolor{tablerow}
  FR-06 & Users shall be able to create, view, update, and delete their own transactions. \\
  FR-07 & Each transaction shall include: type, amount, description, category, and date. \\
  \rowcolor{tablerow}
  FR-08 & Users shall only be able to access their own transactions (data isolation). \\
  \bottomrule
\end{tabularx}

\subsection{Financial Goals}

\begin{tabularx}{\textwidth}{C{1.8cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{ID}} & \textcolor{white}{\textbf{Requirement}} \\
  \midrule
  FR-09 & Users shall be able to create financial goals with a name, target amount, monthly contribution, deadline, and icon. \\
  \rowcolor{tablerow}
  FR-10 & Users shall be able to deposit funds toward a goal to track progress. \\
  FR-11 & The system shall calculate and display progress as a percentage of the target amount. \\
  \rowcolor{tablerow}
  FR-12 & Users shall be able to edit or delete existing goals. \\
  \bottomrule
\end{tabularx}

\subsection{Dashboard}

\begin{tabularx}{\textwidth}{C{1.8cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{ID}} & \textcolor{white}{\textbf{Requirement}} \\
  \midrule
  FR-13 & The dashboard shall display aggregated monthly statistics: total income, expenses, savings, and investments. \\
  \rowcolor{tablerow}
  FR-14 & The dashboard shall include a monthly transaction breakdown bar chart. \\
  FR-15 & The dashboard shall include a donut chart showing allocation across transaction types. \\
  \rowcolor{tablerow}
  FR-16 & The dashboard shall display the five most recent transactions. \\
  FR-17 & The dashboard shall show a live count of unread notifications in the navigation. \\
  \bottomrule
\end{tabularx}

\subsection{Financial Forecasting}

\begin{tabularx}{\textwidth}{C{1.8cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{ID}} & \textcolor{white}{\textbf{Requirement}} \\
  \midrule
  FR-18 & The system shall generate a forward-looking financial forecast based on historical transaction patterns. \\
  \rowcolor{tablerow}
  FR-19 & Forecast data shall be presented in a visual chart organized by month. \\
  \bottomrule
\end{tabularx}

\subsection{Financial Insights}

\begin{tabularx}{\textwidth}{C{1.8cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{ID}} & \textcolor{white}{\textbf{Requirement}} \\
  \midrule
  FR-20 & The system shall analyze user transaction data and surface actionable financial recommendations. \\
  \rowcolor{tablerow}
  FR-21 & Insights shall be presented as a list of categorized, human-readable tips. \\
  \bottomrule
\end{tabularx}

\subsection{Notifications}

\begin{tabularx}{\textwidth}{C{1.8cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{ID}} & \textcolor{white}{\textbf{Requirement}} \\
  \midrule
  FR-22 & The system shall automatically generate notifications for: budget exceeded, goal reached, and new income detected. \\
  \rowcolor{tablerow}
  FR-23 & Notifications shall be deduplicated using a unique reference key. \\
  FR-24 & Users shall be able to mark individual or all notifications as read. \\
  \rowcolor{tablerow}
  FR-25 & The system shall display the count of unread notifications in the sidebar. \\
  \bottomrule
\end{tabularx}

\subsection{Reports}

\begin{tabularx}{\textwidth}{C{1.8cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{ID}} & \textcolor{white}{\textbf{Requirement}} \\
  \midrule
  FR-26 & Users shall be able to generate a financial summary report for any selected month. \\
  \rowcolor{tablerow}
  FR-27 & Reports shall be downloadable as \textbf{PDF} (formatted document). \\
  FR-28 & Reports shall be downloadable as \textbf{CSV} (spreadsheet-compatible). \\
  \rowcolor{tablerow}
  FR-29 & Report content shall include income, expense, saving, investment totals and a category-level breakdown. \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{Non-Functional Requirements}

\begin{tabularx}{\textwidth}{C{1.8cm} L{3cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{ID}} &
  \textcolor{white}{\textbf{Category}} &
  \textcolor{white}{\textbf{Requirement}} \\
  \midrule
  NFR-01 & Security       & All endpoints (except \texttt{/register} and \texttt{/login}) shall require JWT authentication. \\
  \rowcolor{tablerow}
  NFR-02 & Security       & User data shall be strictly isolated — users cannot access another user's data. \\
  NFR-03 & Performance    & Dashboard summary API shall respond within 500\,ms for up to 1,000 transactions per user. \\
  \rowcolor{tablerow}
  NFR-04 & Usability      & The UI shall be fully usable on desktop browsers (Chrome, Firefox, Edge). \\
  NFR-05 & Reliability    & The application shall persist all data in a durable SQLite database. \\
  \rowcolor{tablerow}
  NFR-06 & Maintainability & Backend code shall follow a layered architecture: Controller $\rightarrow$ Service $\rightarrow$ Repository. \\
  NFR-07 & Scalability    & The system shall support migration to a production-grade database (e.g., PostgreSQL) with minimal configuration change. \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{System Architecture Overview}

\begin{lstlisting}
+------------------------------------------------------+
|                    User Browser                      |
|         React 18 SPA  (Vite,  Chart.js)              |
+------------------------+-----------------------------+
                         |  HTTPS / REST API
                         |  JWT Bearer Token
+------------------------v-----------------------------+
|             Spring Boot 3.2  (Java 21)               |
|  +------------+  +----------+  +------------------+  |
|  | Controllers|  | Services |  | Spring Security  |  |
|  +------------+  +----------+  | (JWT Auth Filter)|  |
|  +------------------------+    +------------------+  |
|  |   Spring Data JPA      |                          |
|  +-----------+------------+                          |
+-------------|--------------------------------------------+
              |
+-------------v-----------+
|   SQLite Database        |
|   (wealthwise.db)        |
+-------------------------+
\end{lstlisting}

% ════════════════════════════════════════════════════════════════════════════
\section{Data Entities}

\begin{tabularx}{\textwidth}{L{3cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Entity}} & \textcolor{white}{\textbf{Key Fields}} \\
  \midrule
  \textbf{User}         & id, username, fullName, email, password (hashed), createdAt \\
  \rowcolor{tablerow}
  \textbf{Transaction}  & id, userId, type, amount, description, category, date \\
  \textbf{Goal}         & id, userId, name, targetAmount, savedAmount, monthlyContribution, deadline, icon \\
  \rowcolor{tablerow}
  \textbf{Notification} & id, userId, type, title, message, refKey, isRead, createdAt \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{User Stories}

\begin{tabularx}{\textwidth}{C{1.5cm} L{2.5cm} L{4.5cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{ID}} &
  \textcolor{white}{\textbf{As a\ldots}} &
  \textcolor{white}{\textbf{I want to\ldots}} &
  \textcolor{white}{\textbf{So that\ldots}} \\
  \midrule
  US-01 & New user         & Register an account                      & I can securely access my financial data \\
  \rowcolor{tablerow}
  US-02 & Registered user  & Log in with my credentials               & I can view and manage my finances \\
  US-03 & User             & Add and categorize transactions           & I can track where my money goes \\
  \rowcolor{tablerow}
  US-04 & User             & Create financial goals                   & I can stay motivated to save toward targets \\
  US-05 & User             & Deposit toward a goal                    & I can track progress against my target \\
  \rowcolor{tablerow}
  US-06 & User             & View a dashboard with charts             & I can quickly understand my financial status \\
  US-07 & User             & See a financial forecast                 & I can plan for upcoming months \\
  \rowcolor{tablerow}
  US-08 & User             & Receive budget alert notifications       & I can react before overspending \\
  US-09 & User             & Download a monthly report                & I can review my finances offline \\
  \rowcolor{tablerow}
  US-10 & User             & Read financial insights                  & I can improve my financial habits \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{Acceptance Criteria}

\begin{tabularx}{\textwidth}{L{2.5cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{User Story}} &
  \textcolor{white}{\textbf{Acceptance Criteria}} \\
  \midrule
  US-03 (Add Transaction)
    & Given a logged-in user, when they submit a valid transaction form, then the transaction appears in the list and totals update on the dashboard. \\
  \rowcolor{tablerow}
  US-04 (Create Goal)
    & Given a logged-in user, when they create a goal with a name and target amount, then the goal appears with 0\% progress. \\
  US-05 (Deposit to Goal)
    & Given an existing goal, when the user deposits an amount, then savedAmount increases and the progress percentage updates correctly. \\
  \rowcolor{tablerow}
  US-08 (Budget Alert)
    & Given a user whose expenses exceed income for the month, when they fetch notifications, then a BUDGET\_EXCEEDED notification is present. \\
  US-09 (Download Report)
    & Given a logged-in user, when they request a PDF report for a given month, then a valid PDF file is returned with correct financial totals. \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{Assumptions \& Constraints}

\begin{tabularx}{\textwidth}{C{1.5cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{ID}} & \textcolor{white}{\textbf{Description}} \\
  \midrule
  A-01 & The application targets individual (single-user) financial management, not multi-user households. \\
  \rowcolor{tablerow}
  A-02 & All monetary values are in USD. \\
  A-03 & SQLite is used for development; a production deployment may require a different database. \\
  \rowcolor{tablerow}
  A-04 & Users are responsible for entering their own transaction data (no bank sync). \\
  C-01 & The frontend runs on \texttt{localhost:5173} during development. \\
  \rowcolor{tablerow}
  C-02 & The backend runs on \texttt{localhost:8080} during development. \\
  C-03 & JWT tokens expire after 24 hours and are not refreshed automatically. \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{Glossary}

\begin{tabularx}{\textwidth}{L{3cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Term}} & \textcolor{white}{\textbf{Definition}} \\
  \midrule
  \textbf{Transaction}   & A financial event — Income, Expense, Saving, or Investment. \\
  \rowcolor{tablerow}
  \textbf{Goal}          & A savings target with a defined amount and deadline. \\
  \textbf{Forecast}      & A projected view of future finances based on past trends. \\
  \rowcolor{tablerow}
  \textbf{Insight}       & An automated financial recommendation or observation. \\
  \textbf{Notification}  & A system-generated alert triggered by a financial event. \\
  \rowcolor{tablerow}
  \textbf{JWT}           & JSON Web Token — a stateless authentication mechanism. \\
  \textbf{BRD}           & Business Requirements Document. \\
  \bottomrule
\end{tabularx}

\vfill
\begin{center}
  {\color{primaryblue}\rule{0.5\linewidth}{0.4pt}}\\[0.3cm]
  {\small\color{darkgray} WealthWise --- Business Requirements Document v1.0 --- Confidential}
\end{center}

\end{document}
```
