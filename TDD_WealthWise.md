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
\usepackage{tikz}
\usepackage{forest}

% ── Colors ──────────────────────────────────────────────────────────────────
\definecolor{primaryblue}{RGB}{30, 90, 200}
\definecolor{lightblue}{RGB}{220, 235, 255}
\definecolor{darkgray}{RGB}{50, 50, 50}
\definecolor{lightgray}{RGB}{245, 245, 245}
\definecolor{tableheader}{RGB}{30, 90, 200}
\definecolor{tablerow}{RGB}{240, 245, 255}
\definecolor{codebg}{RGB}{248, 248, 248}
\definecolor{commentgreen}{RGB}{0, 128, 0}
\definecolor{stringred}{RGB}{163, 21, 21}
\definecolor{keywordblue}{RGB}{0, 0, 255}

% ── Hyperref Setup ───────────────────────────────────────────────────────────
\hypersetup{
    colorlinks=true,
    linkcolor=primaryblue,
    urlcolor=primaryblue,
    pdftitle={TDD - WealthWise Financial Tracker},
    pdfauthor={WealthWise Team},
}

% ── Section Styling ──────────────────────────────────────────────────────────
\titleformat{\section}
  {\color{primaryblue}\large\bfseries}
  {\thesection.}{0.5em}{}[\titlerule]

\titleformat{\subsection}
  {\color{darkgray}\normalsize\bfseries}
  {\thesubsection.}{0.5em}{}

\titleformat{\subsubsection}
  {\color{darkgray}\small\bfseries}
  {\thesubsubsection.}{0.5em}{}

% ── Header / Footer ──────────────────────────────────────────────────────────
\pagestyle{fancy}
\fancyhf{}
\rhead{\textcolor{primaryblue}{\textbf{WealthWise TDD}}}
\lhead{\textcolor{darkgray}{Technical Design Document}}
\rfoot{\textcolor{darkgray}{\thepage}}
\lfoot{\textcolor{darkgray}{Confidential — v1.0}}
\renewcommand{\headrulewidth}{0.4pt}
\renewcommand{\footrulewidth}{0.4pt}

% ── Table helpers ────────────────────────────────────────────────────────────
\newcolumntype{L}[1]{>{\raggedright\arraybackslash}p{#1}}
\newcolumntype{C}[1]{>{\centering\arraybackslash}p{#1}}

% ── Code Listing Style ───────────────────────────────────────────────────────
\lstdefinestyle{javaStyle}{
  language=Java,
  basicstyle=\ttfamily\footnotesize,
  backgroundcolor=\color{codebg},
  keywordstyle=\color{keywordblue}\bfseries,
  commentstyle=\color{commentgreen},
  stringstyle=\color{stringred},
  frame=single,
  framesep=4pt,
  breaklines=true,
  showstringspaces=false,
  numbers=left,
  numberstyle=\tiny\color{darkgray},
  numbersep=5pt,
}

\lstdefinestyle{jsonStyle}{
  basicstyle=\ttfamily\footnotesize,
  backgroundcolor=\color{codebg},
  frame=single,
  framesep=4pt,
  breaklines=true,
  showstringspaces=false,
}

\lstdefinestyle{plainStyle}{
  basicstyle=\ttfamily\small,
  backgroundcolor=\color{lightgray},
  frame=single,
  framesep=4pt,
  breaklines=true,
}

\lstset{style=plainStyle}

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

  {\LARGE\bfseries Technical Design Document}\\[2cm]

  \begin{tabular}{L{5cm} L{7cm}}
    \rowcolor{lightblue}
    \textbf{Document Version} & 1.0 \\
    \textbf{Date}             & March 9, 2026 \\
    \rowcolor{lightblue}
    \textbf{Status}           & Draft \\
    \textbf{Project Name}     & WealthWise \\
    \rowcolor{lightblue}
    \textbf{Document Type}    & Technical Design Document \\
    \textbf{Related BRD}      & BRD\_WealthWise v1.0 \\
  \end{tabular}

  \vfill
  {\small\color{darkgray} Confidential — Internal Use Only}
\end{titlepage}

% ── Table of Contents ────────────────────────────────────────────────────────
\tableofcontents
\newpage

% ════════════════════════════════════════════════════════════════════════════
\section{Introduction}

\subsection{Purpose}
This Technical Design Document (TDD) describes the internal architecture, component
design, data models, API contracts, and implementation decisions for the WealthWise
personal financial tracker application. It serves as the authoritative reference for
the development team during implementation and maintenance.

\subsection{Scope}
This document covers the full-stack design of WealthWise, including:
\begin{itemize}[leftmargin=1.5em]
  \item Backend: Java 21 / Spring Boot 3.2 REST API
  \item Frontend: React 18 Single-Page Application (Vite)
  \item Database: SQLite with Spring Data JPA / Hibernate 6
  \item Security: JWT-based stateless authentication
  \item Reporting: PDF and CSV generation
\end{itemize}

\subsection{References}
\begin{itemize}[leftmargin=1.5em]
  \item BRD\_WealthWise v1.0
  \item Spring Boot 3.2 Reference Documentation
  \item React 18 Official Documentation
  \item OWASP Top 10 Security Guidelines
\end{itemize}

% ════════════════════════════════════════════════════════════════════════════
\section{System Architecture}

\subsection{High-Level Architecture}

WealthWise follows a classic \textbf{three-tier architecture}:

\begin{lstlisting}
+----------------------------------------------------------+
|                  PRESENTATION TIER                       |
|      React 18 SPA  |  Vite 5  |  Chart.js 4             |
|      Pages: Auth, Dashboard, Transactions, Goals,        |
|             Forecast, Insights, Notifications, Reports   |
+---------------------------+------------------------------+
                            |  REST / JSON
                            |  Authorization: Bearer <JWT>
+---------------------------v------------------------------+
|                  APPLICATION TIER                        |
|      Spring Boot 3.2  (Java 21)                         |
|  +---------------+  +-------------+  +---------------+  |
|  |  Controllers  |->|  Services   |->| Repositories  |  |
|  +---------------+  +-------------+  +-------+-------+  |
|  +-------------------------------+           |           |
|  |  Spring Security + JwtFilter  |           |           |
|  +-------------------------------+           |           |
+--------------------------------------------|-----------+
                                             |
+--------------------------------------------v-----------+
|                    DATA TIER                             |
|      SQLite 3  (wealthwise.db)                          |
|      Tables: users, transactions, goals, notifications  |
+----------------------------------------------------------+
\end{lstlisting}

\subsection{Component Interaction}

\begin{lstlisting}
Browser
  |
  |-- HTTP Request (JWT in Authorization header)
  v
JwtAuthFilter           (validates token, sets SecurityContext)
  |
  v
Controller              (validates request DTO, delegates)
  |
  v
Service                 (business logic, orchestration)
  |
  v
Repository              (Spring Data JPA interface)
  |
  v
SQLite DB               (file: ./data/wealthwise.db)
\end{lstlisting}

% ════════════════════════════════════════════════════════════════════════════
\section{Technology Stack}

\begin{tabularx}{\textwidth}{L{3.5cm} L{4cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Layer}} &
  \textcolor{white}{\textbf{Technology}} &
  \textcolor{white}{\textbf{Version / Notes}} \\
  \midrule
  Runtime         & Java            & 21 (LTS) \\
  \rowcolor{tablerow}
  Framework       & Spring Boot     & 3.2.0 \\
  Web             & Spring Web MVC  & Embedded Tomcat \\
  \rowcolor{tablerow}
  Security        & Spring Security & JWT via jjwt 0.11.5 \\
  Persistence     & Spring Data JPA & Hibernate 6 ORM \\
  \rowcolor{tablerow}
  Database        & SQLite          & 3.45.1.0 (file-based) \\
  Build           & Maven           & 3.x \\
  \rowcolor{tablerow}
  PDF Generation  & OpenPDF         & 1.3.30 \\
  Frontend        & React           & 18.2.0 \\
  \rowcolor{tablerow}
  Build Tool      & Vite            & 5.0.8 \\
  Routing         & React Router    & DOM 6.21.1 \\
  \rowcolor{tablerow}
  Charts          & Chart.js        & 4.4.0 \\
  Styling         & Plain CSS       & Custom, no framework \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{Project Structure}

\subsection{Backend Structure}

\begin{lstlisting}
backend/
  src/main/java/com/wealthwise/
    config/
      DataLoader.java          # Seeds demo data on startup
      SecurityConfig.java      # Spring Security configuration
    controller/
      AuthController.java      # POST /api/auth/register, /login
      DashboardController.java # GET  /api/dashboard/summary
      ForecastController.java  # GET  /api/forecast
      GoalController.java      # CRUD /api/goals
      InsightController.java   # GET  /api/insights
      NotificationController.java # GET/PATCH /api/notifications
      ReportController.java    # GET  /api/reports (pdf, csv)
      TransactionController.java  # CRUD /api/transactions
    dto/
      AuthDtos.java            # LoginRequest, RegisterRequest,
                               #   AuthResponse, UserSummary
      DashboardResponse.java
      ForecastResponse.java
      GoalRequest.java / GoalResponse.java
      InsightResponse.java
      NotificationResponse.java
      ReportSummaryResponse.java
      TransactionRequest.java / TransactionResponse.java
    exception/
      GlobalExceptionHandler.java  # @ControllerAdvice
    model/
      User.java
      Transaction.java         # enum TransactionType
      Goal.java
      Notification.java        # enum NotificationType
    repository/
      UserRepository.java
      TransactionRepository.java
      GoalRepository.java
      NotificationRepository.java
    security/
      JwtAuthFilter.java       # OncePerRequestFilter
      JwtUtils.java            # Token generation & validation
    service/
      DashboardService.java
      ForecastService.java
      GoalService.java
      InsightService.java
      NotificationService.java
      ReportService.java
      TransactionService.java
    WealthWiseApplication.java
  src/main/resources/
    application.properties
  data/
    wealthwise.db
  pom.xml
\end{lstlisting}

\subsection{Frontend Structure}

\begin{lstlisting}
frontend/
  src/
    api/
      auth.js           # register(), login()
      transactions.js   # getAll(), create(), update(), remove()
      goals.js          # getAll(), create(), update(),
                        #   deposit(), remove()
      dashboard.js      # getSummary()
      forecast.js       # getForecast()
      insights.js       # getInsights()
      notifications.js  # getAll(), getUnreadCount(),
                        #   markRead(), markAllRead()
      reports.js        # getSummary(), getPdf(), getCsv()
    context/
      AuthContext.jsx   # JWT storage, login/logout helpers
    components/
      Sidebar.jsx
      StatCard.jsx
      BreakdownChart.jsx
      DonutChart.jsx
      ForecastChart.jsx
      RecentTransactions.jsx
      TransactionModal.jsx
      GoalModal.jsx
      DeleteModal.jsx
    pages/
      AuthPage.jsx
      DashboardPage.jsx
      TransactionsPage.jsx
      GoalsPage.jsx
      ForecastPage.jsx
      InsightsPage.jsx
      NotificationsPage.jsx
      ReportsPage.jsx
    App.jsx             # Route definitions
    index.css
  index.html
  vite.config.js
  package.json
\end{lstlisting}

% ════════════════════════════════════════════════════════════════════════════
\section{Database Design}

\subsection{Entity-Relationship Overview}

\begin{lstlisting}
users (1) ──< transactions (N)
users (1) ──< goals        (N)
users (1) ──< notifications(N)
\end{lstlisting}

\subsection{Table Definitions}

\subsubsection{users}

\begin{tabularx}{\textwidth}{L{3cm} L{3cm} L{2cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Column}} &
  \textcolor{white}{\textbf{Type}} &
  \textcolor{white}{\textbf{Constraint}} &
  \textcolor{white}{\textbf{Notes}} \\
  \midrule
  id         & BIGINT    & PK, AUTO & Surrogate key \\
  \rowcolor{tablerow}
  username   & VARCHAR   & UNIQUE, NOT NULL & Login identifier \\
  password   & VARCHAR   & NOT NULL & BCrypt hash \\
  \rowcolor{tablerow}
  full\_name & VARCHAR   & NOT NULL & Display name \\
  email      & VARCHAR   & UNIQUE, NOT NULL & Contact email \\
  \rowcolor{tablerow}
  created\_at & TIMESTAMP & NOT NULL & Set on insert \\
  \bottomrule
\end{tabularx}

\subsubsection{transactions}

\begin{tabularx}{\textwidth}{L{3cm} L{3cm} L{2cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Column}} &
  \textcolor{white}{\textbf{Type}} &
  \textcolor{white}{\textbf{Constraint}} &
  \textcolor{white}{\textbf{Notes}} \\
  \midrule
  id          & BIGINT    & PK, AUTO & Surrogate key \\
  \rowcolor{tablerow}
  user\_id    & BIGINT    & FK $\rightarrow$ users.id & Owner \\
  type        & VARCHAR   & NOT NULL & INCOME / EXPENSE / SAVING / INVESTMENT \\
  \rowcolor{tablerow}
  amount      & DECIMAL   & NOT NULL & Precision: 19,2 \\
  description & VARCHAR   & NOT NULL & Free text \\
  \rowcolor{tablerow}
  category    & VARCHAR   & NOT NULL & e.g., Food, Rent \\
  date        & DATE      & NOT NULL & Transaction date \\
  \rowcolor{tablerow}
  created\_at & TIMESTAMP & NOT NULL & Set on insert \\
  \bottomrule
\end{tabularx}

\subsubsection{goals}

\begin{tabularx}{\textwidth}{L{3.5cm} L{2.8cm} L{2cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Column}} &
  \textcolor{white}{\textbf{Type}} &
  \textcolor{white}{\textbf{Constraint}} &
  \textcolor{white}{\textbf{Notes}} \\
  \midrule
  id                    & BIGINT  & PK, AUTO & \\
  \rowcolor{tablerow}
  user\_id              & BIGINT  & FK $\rightarrow$ users.id & \\
  name                  & VARCHAR & NOT NULL & Goal label \\
  \rowcolor{tablerow}
  target\_amount        & DECIMAL & NOT NULL & Final goal amount \\
  saved\_amount         & DECIMAL & NOT NULL & Running total (default 0) \\
  \rowcolor{tablerow}
  monthly\_contribution & DECIMAL & NOT NULL & Planned monthly deposit \\
  deadline              & DATE    & NOT NULL & Target completion date \\
  \rowcolor{tablerow}
  icon                  & VARCHAR & NULLABLE & Emoji or icon code \\
  created\_at           & TIMESTAMP & NOT NULL & \\
  \bottomrule
\end{tabularx}

\subsubsection{notifications}

\begin{tabularx}{\textwidth}{L{3cm} L{2.8cm} L{2cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Column}} &
  \textcolor{white}{\textbf{Type}} &
  \textcolor{white}{\textbf{Constraint}} &
  \textcolor{white}{\textbf{Notes}} \\
  \midrule
  id         & BIGINT    & PK, AUTO & \\
  \rowcolor{tablerow}
  user\_id   & BIGINT    & FK $\rightarrow$ users.id & \\
  type       & VARCHAR   & NOT NULL & BUDGET\_EXCEEDED / GOAL\_REACHED / INCOME\_DETECTED \\
  \rowcolor{tablerow}
  title      & VARCHAR   & NOT NULL & Short heading \\
  message    & VARCHAR   & NOT NULL & Full alert text \\
  \rowcolor{tablerow}
  ref\_key   & VARCHAR   & UNIQUE per user & Deduplication key \\
  is\_read   & BOOLEAN   & NOT NULL, default false & \\
  \rowcolor{tablerow}
  created\_at & TIMESTAMP & NOT NULL & \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{Domain Models (JPA Entities)}

\subsection{User Entity}

\begin{lstlisting}[style=javaStyle]
@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;          // BCrypt hash

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "created_at", nullable = false,
            updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Transaction> transactions;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Goal> goals;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Notification> notifications;
}
\end{lstlisting}

\subsection{Transaction Entity}

\begin{lstlisting}[style=javaStyle]
@Entity
@Table(name = "transactions")
public class Transaction {

    public enum TransactionType {
        INCOME, EXPENSE, SAVING, INVESTMENT
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private LocalDate date;

    @Column(name = "created_at", nullable = false,
            updatable = false)
    private LocalDateTime createdAt;
}
\end{lstlisting}

\subsection{Goal Entity}

\begin{lstlisting}[style=javaStyle]
@Entity
@Table(name = "goals")
public class Goal {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(name = "target_amount", nullable = false,
            precision = 19, scale = 2)
    private BigDecimal targetAmount;

    @Column(name = "saved_amount", nullable = false,
            precision = 19, scale = 2)
    private BigDecimal savedAmount = BigDecimal.ZERO;

    @Column(name = "monthly_contribution",
            nullable = false, precision = 19, scale = 2)
    private BigDecimal monthlyContribution;

    @Column(nullable = false)
    private LocalDate deadline;

    private String icon;

    @Column(name = "created_at", nullable = false,
            updatable = false)
    private LocalDateTime createdAt;
}
\end{lstlisting}

\subsection{Notification Entity}

\begin{lstlisting}[style=javaStyle]
@Entity
@Table(name = "notifications")
public class Notification {

    public enum NotificationType {
        BUDGET_EXCEEDED, GOAL_REACHED, INCOME_DETECTED
    }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(name = "ref_key")
    private String refKey;           // deduplication

    @Column(name = "is_read", nullable = false)
    private boolean read = false;

    @Column(name = "created_at", nullable = false,
            updatable = false)
    private LocalDateTime createdAt;
}
\end{lstlisting}

% ════════════════════════════════════════════════════════════════════════════
\section{REST API Contract}

\subsection{Base URL \& Headers}

\begin{tabularx}{\textwidth}{L{3.5cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Property}} & \textcolor{white}{\textbf{Value}} \\
  \midrule
  Base URL (dev)      & \texttt{http://localhost:8080/api} \\
  \rowcolor{tablerow}
  Content-Type        & \texttt{application/json} \\
  Auth Header         & \texttt{Authorization: Bearer \{token\}} \\
  \rowcolor{tablerow}
  Token Lifetime      & 24 hours \\
  \bottomrule
\end{tabularx}

\subsection{Authentication Endpoints}

\begin{tabularx}{\textwidth}{L{1.5cm} L{4cm} L{3.5cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Method}} &
  \textcolor{white}{\textbf{Path}} &
  \textcolor{white}{\textbf{Auth}} &
  \textcolor{white}{\textbf{Description}} \\
  \midrule
  POST & \texttt{/auth/register} & None & Register new user \\
  \rowcolor{tablerow}
  POST & \texttt{/auth/login}    & None & Login, returns JWT \\
  \bottomrule
\end{tabularx}

\textbf{Register Request Body:}
\begin{lstlisting}[style=jsonStyle]
{
  "username": "john_doe",
  "password": "secret123",
  "fullName": "John Doe",
  "email": "john@example.com"
}
\end{lstlisting}

\textbf{Login Response Body:}
\begin{lstlisting}[style=jsonStyle]
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "user": {
    "id": 1,
    "username": "john_doe",
    "fullName": "John Doe",
    "email": "john@example.com"
  }
}
\end{lstlisting}

\subsection{Transaction Endpoints}

\begin{tabularx}{\textwidth}{L{1.5cm} L{4.5cm} L{2.5cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Method}} &
  \textcolor{white}{\textbf{Path}} &
  \textcolor{white}{\textbf{Auth}} &
  \textcolor{white}{\textbf{Description}} \\
  \midrule
  GET    & \texttt{/transactions}      & Bearer & Get all user transactions \\
  \rowcolor{tablerow}
  POST   & \texttt{/transactions}      & Bearer & Create transaction \\
  PUT    & \texttt{/transactions/\{id\}} & Bearer & Update transaction \\
  \rowcolor{tablerow}
  DELETE & \texttt{/transactions/\{id\}} & Bearer & Delete transaction \\
  \bottomrule
\end{tabularx}

\textbf{Transaction Request/Response Body:}
\begin{lstlisting}[style=jsonStyle]
{
  "id": 1,
  "type": "EXPENSE",
  "amount": 150.00,
  "description": "Grocery shopping",
  "category": "Food",
  "date": "2026-03-01"
}
\end{lstlisting}

\subsection{Goal Endpoints}

\begin{tabularx}{\textwidth}{L{1.5cm} L{5cm} L{2cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Method}} &
  \textcolor{white}{\textbf{Path}} &
  \textcolor{white}{\textbf{Auth}} &
  \textcolor{white}{\textbf{Description}} \\
  \midrule
  GET    & \texttt{/goals}               & Bearer & Get all goals \\
  \rowcolor{tablerow}
  POST   & \texttt{/goals}               & Bearer & Create goal \\
  PUT    & \texttt{/goals/\{id\}}         & Bearer & Update goal \\
  \rowcolor{tablerow}
  PATCH  & \texttt{/goals/\{id\}/deposit} & Bearer & Add deposit \\
  DELETE & \texttt{/goals/\{id\}}         & Bearer & Delete goal \\
  \bottomrule
\end{tabularx}

\subsection{Other Endpoints}

\begin{tabularx}{\textwidth}{L{1.5cm} L{5.5cm} L{2cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Method}} &
  \textcolor{white}{\textbf{Path}} &
  \textcolor{white}{\textbf{Auth}} &
  \textcolor{white}{\textbf{Description}} \\
  \midrule
  GET   & \texttt{/dashboard/summary}         & Bearer & Monthly stats \\
  \rowcolor{tablerow}
  GET   & \texttt{/forecast}                  & Bearer & Monthly forecast \\
  GET   & \texttt{/insights}                  & Bearer & AI-style tips \\
  \rowcolor{tablerow}
  GET   & \texttt{/notifications}             & Bearer & All notifications \\
  GET   & \texttt{/notifications/unread-count}& Bearer & Unread count \\
  \rowcolor{tablerow}
  PATCH & \texttt{/notifications/\{id\}/read} & Bearer & Mark one read \\
  PATCH & \texttt{/notifications/read-all}    & Bearer & Mark all read \\
  \rowcolor{tablerow}
  GET   & \texttt{/reports/summary?month=}    & Bearer & JSON summary \\
  GET   & \texttt{/reports/pdf?month=}        & Bearer & PDF download \\
  \rowcolor{tablerow}
  GET   & \texttt{/reports/csv?month=}        & Bearer & CSV download \\
  \bottomrule
\end{tabularx}

\subsection{HTTP Status Codes}

\begin{tabularx}{\textwidth}{C{2.5cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Status}} & \textcolor{white}{\textbf{Meaning}} \\
  \midrule
  200 OK         & Successful GET, PUT, PATCH \\
  \rowcolor{tablerow}
  201 Created    & Successful POST (resource created) \\
  204 No Content & Successful DELETE \\
  \rowcolor{tablerow}
  400 Bad Request & Validation failed \\
  401 Unauthorized & Missing or invalid JWT token \\
  \rowcolor{tablerow}
  403 Forbidden  & Authenticated but not authorized \\
  404 Not Found  & Resource does not exist \\
  \rowcolor{tablerow}
  500 Internal Server Error & Unexpected server failure \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{Security Design}

\subsection{Authentication Flow}

\begin{lstlisting}
Client                    JwtAuthFilter         SecurityContext
  |                            |                      |
  |-- POST /auth/login ------> |                      |
  |                            |  (public endpoint)   |
  |<-- 200 { token: "..." } -- |                      |
  |                            |                      |
  |-- GET /api/transactions --> |                      |
  |   Authorization: Bearer T  |                      |
  |                            |-- validateToken() --> |
  |                            |                      |-- setAuth()
  |                            |                      |
  |<-- 200 [...] ------------- |<--------------------- |
\end{lstlisting}

\subsection{JWT Configuration}

\begin{tabularx}{\textwidth}{L{4cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Property}} & \textcolor{white}{\textbf{Value}} \\
  \midrule
  Algorithm       & HS256 (HMAC-SHA256) \\
  \rowcolor{tablerow}
  Expiry          & 86,400,000 ms (24 hours) \\
  Claims          & \texttt{sub} (username), \texttt{iat}, \texttt{exp} \\
  \rowcolor{tablerow}
  Secret Storage  & \texttt{application.properties} (env variable in prod) \\
  Library         & jjwt 0.11.5 \\
  \bottomrule
\end{tabularx}

\subsection{Password Security}

\begin{itemize}[leftmargin=1.5em]
  \item Passwords are hashed using \textbf{BCrypt} via Spring Security's \texttt{PasswordEncoder}.
  \item Plain-text passwords are never stored or returned in any API response.
  \item Minimum password policy enforcement is handled at the DTO validation layer (\texttt{@Size}, \texttt{@NotBlank}).
\end{itemize}

\subsection{CORS Configuration}

\begin{tabularx}{\textwidth}{L{4cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Property}} & \textcolor{white}{\textbf{Value}} \\
  \midrule
  Allowed Origins & \texttt{http://localhost:5173}, \texttt{http://localhost:3000} \\
  \rowcolor{tablerow}
  Allowed Methods & GET, POST, PUT, PATCH, DELETE, OPTIONS \\
  Allowed Headers & Authorization, Content-Type \\
  \rowcolor{tablerow}
  Allow Credentials & true \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{Service Layer Design}

\subsection{DashboardService}

Aggregates transaction data for the authenticated user in the current calendar month.
Returns totals per \texttt{TransactionType} and the five most recent transactions.

\subsection{ForecastService}

Calculates a rolling 3-month average for each \texttt{TransactionType} using historical
data, then projects the next 6 months using linear extrapolation.
Results are grouped by month label (\texttt{MMM yyyy}).

\subsection{InsightService}

Applies rule-based analysis to the current month's transactions:
\begin{itemize}[leftmargin=1.5em]
  \item Savings rate below 20\% of income $\Rightarrow$ savings tip
  \item Expenses exceed income $\Rightarrow$ budget warning
  \item No investment transactions $\Rightarrow$ investment prompt
  \item Largest spending category $\Rightarrow$ category-specific recommendation
\end{itemize}

\subsection{NotificationService}

Triggered on every \texttt{GET /notifications} call. Runs three checks and creates
notifications only when the corresponding \texttt{refKey} does not already exist:

\begin{tabularx}{\textwidth}{L{4cm} L{4cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Type}} &
  \textcolor{white}{\textbf{Trigger Condition}} &
  \textcolor{white}{\textbf{refKey Pattern}} \\
  \midrule
  BUDGET\_EXCEEDED  & expenses $>$ income (current month) & \texttt{budget-YYYY-MM} \\
  \rowcolor{tablerow}
  GOAL\_REACHED     & savedAmount $\geq$ targetAmount      & \texttt{goal-\{goalId\}} \\
  INCOME\_DETECTED  & new income transaction exists        & \texttt{income-\{txId\}} \\
  \bottomrule
\end{tabularx}

\subsection{ReportService}

Generates monthly reports with two output formats:
\begin{itemize}[leftmargin=1.5em]
  \item \textbf{PDF}: Uses OpenPDF to produce a formatted A4 document with a title, summary table, and category-level breakdown table.
  \item \textbf{CSV}: Writes a comma-separated file with headers: \texttt{Category, Type, Total}.
\end{itemize}
Both accept a \texttt{month} query parameter in \texttt{YYYY-MM} format.

% ════════════════════════════════════════════════════════════════════════════
\section{Frontend Design}

\subsection{State Management}

The frontend uses \textbf{React Context} (\texttt{AuthContext}) for global authentication state.
All other UI state is local component state managed with \texttt{useState} and \texttt{useEffect}.

\begin{tabularx}{\textwidth}{L{4cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Context Value}} & \textcolor{white}{\textbf{Description}} \\
  \midrule
  \texttt{user}       & Logged-in user object (id, username, fullName) \\
  \rowcolor{tablerow}
  \texttt{token}      & JWT string stored in \texttt{localStorage} \\
  \texttt{login()}    & Stores token and user, redirects to dashboard \\
  \rowcolor{tablerow}
  \texttt{logout()}   & Clears token and user, redirects to \texttt{/auth} \\
  \bottomrule
\end{tabularx}

\subsection{Routing}

\begin{tabularx}{\textwidth}{L{4cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Path}} & \textcolor{white}{\textbf{Component}} \\
  \midrule
  \texttt{/auth}           & AuthPage (public) \\
  \rowcolor{tablerow}
  \texttt{/}               & DashboardPage (protected) \\
  \texttt{/transactions}   & TransactionsPage (protected) \\
  \rowcolor{tablerow}
  \texttt{/goals}          & GoalsPage (protected) \\
  \texttt{/forecast}       & ForecastPage (protected) \\
  \rowcolor{tablerow}
  \texttt{/insights}       & InsightsPage (protected) \\
  \texttt{/notifications}  & NotificationsPage (protected) \\
  \rowcolor{tablerow}
  \texttt{/reports}        & ReportsPage (protected) \\
  \bottomrule
\end{tabularx}

Protected routes check for a valid token in \texttt{AuthContext}; unauthenticated users
are redirected to \texttt{/auth}.

\subsection{API Layer}

All API modules share a common pattern:
\begin{itemize}[leftmargin=1.5em]
  \item Base URL: \texttt{http://localhost:8080/api}
  \item Token injected via \texttt{Authorization: Bearer \{token\}} header on every request
  \item On 401 response: \texttt{AuthContext.logout()} is called and the user is redirected to \texttt{/auth}
\end{itemize}

\subsection{Key Components}

\begin{tabularx}{\textwidth}{L{4.5cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Component}} & \textcolor{white}{\textbf{Responsibility}} \\
  \midrule
  \texttt{Sidebar}             & Navigation links, unread notification badge \\
  \rowcolor{tablerow}
  \texttt{StatCard}            & Displays a single metric (label + value) \\
  \texttt{BreakdownChart}      & Bar chart — monthly income/expense/saving/investment \\
  \rowcolor{tablerow}
  \texttt{DonutChart}          & Donut chart — allocation across transaction types \\
  \texttt{ForecastChart}       & Line chart — projected monthly totals \\
  \rowcolor{tablerow}
  \texttt{RecentTransactions}  & Table of the 5 most recent transactions \\
  \texttt{TransactionModal}    & Create / edit transaction form (modal) \\
  \rowcolor{tablerow}
  \texttt{GoalModal}           & Create / edit goal form (modal) \\
  \texttt{DeleteModal}         & Confirmation dialog for delete actions \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{Configuration}

\subsection{Backend — application.properties}

\begin{lstlisting}
# Server
server.port=8080

# SQLite DataSource
spring.datasource.url=jdbc:sqlite:./data/wealthwise.db
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.jpa.database-platform=
    org.hibernate.community.dialect.SQLiteDialect
spring.jpa.hibernate.ddl-auto=update

# JWT
app.jwt.secret=<base64-encoded-secret>
app.jwt.expiration-ms=86400000
\end{lstlisting}

\subsection{Frontend — vite.config.js}

\begin{lstlisting}
export default {
  server: {
    port: 5173,
  }
}
\end{lstlisting}

% ════════════════════════════════════════════════════════════════════════════
\section{Error Handling}

\subsection{Backend}

A \texttt{GlobalExceptionHandler} annotated with \texttt{@ControllerAdvice} intercepts all
unhandled exceptions and returns structured JSON error responses:

\begin{lstlisting}[style=jsonStyle]
{
  "status": 400,
  "error": "Bad Request",
  "message": "amount must be greater than 0",
  "timestamp": "2026-03-09T10:00:00"
}
\end{lstlisting}

\begin{tabularx}{\textwidth}{L{5cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Exception}} & \textcolor{white}{\textbf{HTTP Status}} \\
  \midrule
  \texttt{MethodArgumentNotValidException} & 400 Bad Request \\
  \rowcolor{tablerow}
  \texttt{BadCredentialsException}         & 401 Unauthorized \\
  \texttt{AccessDeniedException}           & 403 Forbidden \\
  \rowcolor{tablerow}
  \texttt{EntityNotFoundException}         & 404 Not Found \\
  \texttt{Exception} (catch-all)           & 500 Internal Server Error \\
  \bottomrule
\end{tabularx}

\subsection{Frontend}

\begin{itemize}[leftmargin=1.5em]
  \item API errors display inline error messages within forms or toast-style alerts.
  \item 401 responses trigger an automatic logout and redirect to \texttt{/auth}.
  \item Loading states are managed per-component using a \texttt{loading} boolean state variable.
\end{itemize}

% ════════════════════════════════════════════════════════════════════════════
\section{Build \& Deployment}

\subsection{Backend Build}

\begin{lstlisting}
# Compile and package
mvn clean package -DskipTests

# Run
java -jar target/wealthwise-0.0.1-SNAPSHOT.jar
\end{lstlisting}

\subsection{Frontend Build}

\begin{lstlisting}
# Install dependencies
npm install

# Development server
npm run dev       # http://localhost:5173

# Production build
npm run build     # Output: dist/
npm run preview   # Preview production build
\end{lstlisting}

\subsection{Environment Variables (Production)}

\begin{tabularx}{\textwidth}{L{5cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Variable}} & \textcolor{white}{\textbf{Description}} \\
  \midrule
  \texttt{APP\_JWT\_SECRET}       & Base64-encoded JWT signing secret \\
  \rowcolor{tablerow}
  \texttt{APP\_JWT\_EXPIRATION}   & Token expiry in milliseconds \\
  \texttt{DATASOURCE\_URL}        & JDBC URL (for prod DB migration) \\
  \rowcolor{tablerow}
  \texttt{VITE\_API\_BASE\_URL}   & Backend base URL for frontend build \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{Design Decisions \& Rationale}

\begin{tabularx}{\textwidth}{L{4cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{Decision}} & \textcolor{white}{\textbf{Rationale}} \\
  \midrule
  SQLite as DB          & Zero-config, file-based, ideal for single-user development. Schema portable to PostgreSQL via \texttt{ddl-auto=update}. \\
  \rowcolor{tablerow}
  JWT (stateless)       & No server-side session storage required. Scales horizontally without shared session state. \\
  React Context (no Redux) & Application state is simple and localized; Redux would add unnecessary complexity. \\
  \rowcolor{tablerow}
  OpenPDF (not iText)   & OpenPDF is a free, open-source fork of iText 2, suitable for PDF generation without licensing concerns. \\
  Plain CSS             & Avoids heavy CSS framework dependencies; gives full control over design without bundle bloat. \\
  \rowcolor{tablerow}
  Rule-based Insights   & Simple, transparent, and fast. No ML model dependency. Easily extendable with new rules. \\
  Notification dedup via refKey & Prevents duplicate alerts for the same event on repeated API calls. \\
  \bottomrule
\end{tabularx}

% ════════════════════════════════════════════════════════════════════════════
\section{Known Limitations \& Future Improvements}

\begin{tabularx}{\textwidth}{L{1.8cm} L{4cm} X}
  \toprule
  \rowcolor{tableheader}
  \textcolor{white}{\textbf{ID}} &
  \textcolor{white}{\textbf{Limitation}} &
  \textcolor{white}{\textbf{Suggested Improvement}} \\
  \midrule
  LIM-01 & SQLite not suitable for production multi-user load & Migrate to PostgreSQL or MySQL \\
  \rowcolor{tablerow}
  LIM-02 & JWT not revocable before expiry & Implement token blacklist or short-lived refresh tokens \\
  LIM-03 & No automated test coverage & Add JUnit 5 unit tests and Spring MockMvc integration tests \\
  \rowcolor{tablerow}
  LIM-04 & Frontend has no pagination & Add server-side pagination for transaction lists \\
  LIM-05 & Forecast is rule-based only & Integrate ML-based time-series forecasting \\
  \rowcolor{tablerow}
  LIM-06 & No bank sync & Integrate open-banking APIs (e.g., Plaid) \\
  LIM-07 & Single currency (USD) & Add multi-currency support with exchange-rate API \\
  \bottomrule
\end{tabularx}

\vfill
\begin{center}
  {\color{primaryblue}\rule{0.5\linewidth}{0.4pt}}\\[0.3cm]
  {\small\color{darkgray} WealthWise --- Technical Design Document v1.0 --- Confidential}
\end{center}

\end{document}
```
