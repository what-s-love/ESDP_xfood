<#assign known = SPRING_SECURITY_CONTEXT??>
<#if known>
    <#assign authorizedUser = SPRING_SECURITY_CONTEXT.authentication>
    <#assign userRole = authorizedUser.authorities[0].authority>
<#else>
</#if>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Expert checks</title>

    <link href="/DataTables/datatables.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
          crossorigin="anonymous">
    <link href="https://cdn.jsdelivr.net/npm/select2@4.1.0-rc.0/dist/css/select2.min.css" rel="stylesheet" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <!-- Fontawesome CDN Link -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.1.1/css/all.min.css">
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Montserrat:ital,wght@0,100..900;1,100..900&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/daterangepicker/daterangepicker.css" />
    <link rel="stylesheet" href="/css/layout.css">
</head>

<body>
<header>
    <nav class="navbar navbar-expand fw-medium bg-body border-bottom " id="navbar">
        <div class="container">
            <a class="navbar-brand fw-semibold " href="#">XFOOD</a>
            <button class="navbar-toggler" type="button" data-bs-toggle="collapse"
                    data-bs-target="#navbarSupportedContent" aria-controls="navbarSupportedContent"
                    aria-expanded="false" aria-label="Toggle navigation">
                <span class="navbar-toggler-icon"></span>
            </button>
            <div class="collapse navbar-collapse" id="navbarSupportedContent">
                <ul class="navbar-nav ms-auto">
                    <#if authorizedUser??>
                        <li class="nav-item dropdown" href="#">
                            <a class="nav-link dropdown-toggle d-flex flex-row flex-nowrap column-gap-2 align-items-center py-1"
                               href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-person-circle fs-5 m-0 p-0 "></i>
                                <span>
                                        ${authorizedUser.name}
                                    </span>
                            </a>
                            <ul class="dropdown-menu dropdown-menu-end">
                                <li><a class="dropdown-item" href="/profile">Мой профиль</a></li>
                                <#if userRole == "ROLE_EXPERT">
                                    <li>
                                        <a class="dropdown-item" href="/my-opportunities">Карта возможностей</a>
                                    </li>
                                <#else>
                                    <li>
                                        <a class="dropdown-item" href="/deadlines">Настройки</a>
                                    </li>
                                </#if>
                                <li>
                                    <hr class="dropdown-divider">
                                </li>
                                <li>
                                    <a class="dropdown-item" href="/logout">Выход</a>
                                </li>
                            </ul>
                        </li>
                    </#if>
                </ul>
            </div>
        </div>
    </nav>
</header>
<main class="container my-4">
    <a href="/analytics" class="btn btn-light btn-sm my-2 px-3"><i class="bi bi-arrow-90deg-left"></i></a>

    <form action="/statistics" method="get" id="period">
        <input type="hidden" name="from" id="from" value="">
        <input type="hidden" name="to" id="to" value="">
        <div id="reportrange" class="form-select w-50 m-3 ms-3 fs-5 shadow-sm" style="cursor: pointer;">
            <i class="bi bi-calendar-fill text-dodo"></i>
            <span></span> <i class="fa fa-caret-down"></i>
        </div>
        <button type="submit" id="date-range-button" class="mx-3 mt-2 mb-1 btn btn-dodo" >Применить</button>
    </form>
    <#if from?? && to??>
        <#assign size = (statistics.days?size) + 3>
        <#assign size1 = (statistics.days?size) + 4>

        <div class="table-responsive">
            <table id="myTable" class="table table-bordered">
                <thead class="fs-6">
                <tr>
                    <th>Менеджер</th>
                    <th>Эксперт</th>
                    <th>Пиццерия</th>
                    <th>Тип</th>
                    <#if statistics.days??>
                        <#list statistics.days as d>
                            <th colspan="fs-6">${d.day}</th>
                        </#list>
                    </#if>
                    <th>Среднее за менеджера и эксперта</th>
                    <th>Среднее за пиццерию</th>
                </tr>
                </thead>
                <tbody>
                <#list statistics.tables as table>
                    <#list table.rows as row>
                        <tr>
                            <td>${row.manager.name} ${row.manager.surname}</td>
                            <td>${row.expert.name} ${row.expert.surname}</td>
                            <td>${row.pizzeria.name}</td>
                            <td>${row.type}</td>
                            <#list statistics.days as d>
                                <#assign cellFound = false>
                                <#list row.cells as cell>
                                    <#if d.day == cell.date>
                                        <td>${cell.points}</td>
                                        <#assign cellFound = true>
                                        <#break >
                                    </#if>
                                </#list>
                                <#if !cellFound>
                                    <td> </td>
                                </#if>
                            </#list>
                            <td>${row.averageByRow}</td>
                            <td class="average-by-pizzeria">${table.averageByTable}</td>
                        </tr>
                    </#list>
                </#list>
                </tbody>

                <tfoot>
                <tr>
                    <th colspan="${size}" style="text-align:right">Общий средний балл по всем пиццериям:</th>
                    <th style="text-align:right" colspan="${size1}">${statistics.average}</th>
                </tr>
                </tfoot>
            </table>
        </div>
    <#else>
        <span class="mx-md-3 my-2">
            Выберите период для отображения статистики
        </span>
    </#if>
</main>
<script src="/js/statistics.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.9.2/dist/umd/popper.min.js"
        integrity="sha384-IQsoLXl5PILFhosVNubq5LC7Qb9DXgDA9i+tQ8Zj3iwWAwPtgFTxbJ8NT4GN1R8p"
        crossorigin="anonymous"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.min.js"
        integrity="sha384-cVKIPhGWiC2Al4u+LWgxfKTRIcfu0JTxR+EQDz/bgldoEyl4H0zUF0QKbrJ0EcQF"
        crossorigin="anonymous"></script>
<script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
<script src="/DataTables/datatables.min.js"></script>
<script>
    $(document).ready(function() {
        $('#myTable').DataTable({
            responsive: false,
            language: {
                url: '/DataTables/ru.json',
            },
            lengthMenu: [
                [10, 25, 50, -1],
                [10, 25, 50, 'All']
            ],
            rowGroup: {
                dataSrc: 2,
                endRender: function(rows) {
                    let totalColumns = rows.columns().header().length;
                    let lastColumnIndex = totalColumns - 2;

                    let averageColumnIndex = rows.columns().indexes().filter(index => {
                        return $(rows.column(index).header()).text().includes('Среднее за пиццерию');
                    })[0];


                    if (averageColumnIndex !== undefined) {
                        $('#myTable').DataTable().column(averageColumnIndex).visible(false);
                    }

                    let avg = rows
                        .data()
                        .pluck(lastColumnIndex)
                        .reduce(function(a, b) {
                            return a + parseFloat(b);
                        }, 0) / rows.count();
                    return $('<tr/>')
                        .append('<td colspan="3">Среднее за пиццерию</td>')
                        .append('<td colspan="' + (rows.columns().header().length - 4) + '">' + avg.toFixed(2) + '</td>');
                }
            },
            layout: {
                topStart: {
                    buttons: [
                        {
                            extend: 'excelHtml5',
                            autoFilter: true,
                            sheetName: 'Статистика',
                            filename: 'Статистика',
                            header: true,
                            footer: true,
                            exportOptions: {
                                modifier: {
                                    search: 'applied',
                                }},
                            text: 'Экспорт в Excel',
                            className: 'btn btn-secondary ms-1'
                        }
                    ]
                }
            }

        });

    })
</script>

<script src="https://cdn.jsdelivr.net/npm/moment/min/moment-with-locales.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/daterangepicker/daterangepicker.min.js"></script>
<script src="https://cdn.datatables.net/2.1.2/js/dataTables.bootstrap5.js"></script>
<script src="https://cdn.datatables.net/buttons/3.1.0/js/buttons.html5.min.js"></script>
<script>
    $(function() {
        moment.locale('ru');
        var start = moment().subtract(29, 'days');
        var end = moment();

        function cb(start, end) {
            $('#reportrange span').html(start.format('MMMM D, YYYY') + ' - ' + end.format('MMMM D, YYYY'));
            $('#from').val(start.format('YYYY-MM-DD'));
            $('#to').val(end.format('YYYY-MM-DD'));
        }

        $('#reportrange').daterangepicker({
            startDate: start,
            endDate: end,
            ranges: {
                'Сегодня': [moment(), moment()],
                'Вчера': [moment().subtract(1, 'days'), moment().subtract(1, 'days')],
                'Последние 7 дней': [moment().subtract(6, 'days'), moment()],
                'Последние 30 дней': [moment().subtract(29, 'days'), moment()],
                'Этот месяц': [moment().startOf('month'), moment().endOf('month')],
                'Последний месяц': [moment().subtract(1, 'month').startOf('month'), moment().subtract(1, 'month').endOf('month')]
            },
            locale: {
                format: 'DD.MM.YYYY',
                separator: ' - ',
                applyLabel: 'Применить',
                cancelLabel: 'Отменить',
                fromLabel: 'С',
                toLabel: 'По',
                customRangeLabel: 'Выбрать диапазон',
                daysOfWeek: ['Вс', 'Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб'],
                monthNames: ['Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь', 'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь'],
                firstDay: 1
            }
        }, cb);

        cb(start, end);

        $('#reportrange').on('apply.daterangepicker', function(ev, picker) {
            start = picker.startDate;
            end = picker.endDate;
            $('#from').val(start.format('YYYY-MM-DD'));
            $('#to').val(end.format('YYYY-MM-DD'));
        });
    });
</script>
</body>
</html>

