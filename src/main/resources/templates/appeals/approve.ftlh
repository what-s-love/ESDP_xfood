<#import "../layout.ftlh" as main>
<link rel="stylesheet" href="/css/profile.css" xmlns="http://www.w3.org/1999/html">
<link rel="stylesheet" href="/css/appeal_approve.css">
<style>
    .rounded-border {
        border-radius: 10px;
        overflow: hidden;
    }
</style>
<@main.layout>
    <div class="container">
        <div class="card border-0 rounded-4 shadow-sm">
            <div class="card-body">
                <#if appeal.status??>
                    <#if appeal.status == true>
                        <div class="alert alert-success d-flex align-items-center" role="alert">
                            <svg class="bi flex-shrink-0 me-2" width="24" height="24" role="img" aria-label="Success:">
                                <use xlink:href="#check-circle-fill"/>
                            </svg>
                            <div>
                                <i class="bi bi-check-square fs-4 text-success"></i> подтверждённая апелляция
                            </div>
                        </div>
                    <#elseif appeal.status == false>
                        <div class="alert alert-danger d-flex align-items-center" role="alert">
                            <div>
                                <i class="bi bi-x-square fs-4 text-danger"></i> отклонённая апелляция
                            </div>
                        </div>
                    </#if>
                </#if>

                <h5 class="card-title">${appeal.checkListsCriteria.pizzeria}</h5>
                <h6 class="card-subtitle mb-2 text-muted">
                    Период: <#if appeal.localDate??> ${appeal.localDate} </#if></h6>
                <p class="card-text">Email: ${appeal.email}</p>
                <p class="card-text">ФИО: ${appeal.fullName}</p>
                <a class="btn btn-success" href="/checks/${appeal.checkListUuid}/result" role="button">Чек-лист</a>
                <p class="card-text">Критерий: </p>
                <table class="table  table-bordered  table-warning my-2  border-warning border-opacity-25  rounded-border">
                    <thead>
                    <tr>
                        <th scope="col">Раздел</th>
                        <th scope="col">Зона</th>
                        <th scope="col">Описание</th>
                        <th scope="col">Макс. значение</th>
                        <th scope="col">Оценка</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td>${appeal.checkListsCriteria.criteria.section}</td>
                        <td>${appeal.checkListsCriteria.criteria.zone}</td>
                        <td>${appeal.checkListsCriteria.criteria.description}</td>
                        <#if appeal.checkListsCriteria.criteria.maxValue?? && appeal.checkListsCriteria.criteria.maxValue != 0>
                            <td>${appeal.checkListsCriteria.criteria.maxValue}</td>
                        <#else>
                            <td>${appeal.checkListsCriteria.criteria.coefficient}</td>
                        </#if>
                        <td>${appeal.checkListsCriteria.criteria.value}</td>
                    </tr>
                    </tbody>
                </table>
                <#if  appeal.remark??>
                    <p class="card-text">Нарушение: <br> ${appeal.remark}</p>
                </#if>
                <p class="card-text">Комментарий менеджера: </p>
                <#if appeal.comment??>
                    <div class="alert alert-warning" role="alert">
                        ${appeal.comment}
                    </div>
                </#if>
                <#if appeal.status??>
                    <#if appeal.respond??>
                        <p class="card-text">Ответ: </p>
                        <div class="alert alert-warning" role="alert">
                            ${appeal.respond}
                        </div>
                    </#if>
                </#if>
            </div>
        </div>
        <div class="card my-3 border-0 rounded-4 shadow-sm">
            <div class="card-body">
                <h3>Файлы:</h3>

                <div class="gallery_functionality">
                    <div class="grid">
                        <#list filenames as filename>
                            <#assign lastDotIndex = filename?last_index_of(".")>
                            <#assign extension = filename?substring(lastDotIndex + 1)>

                            <#if extension == "png" || extension == "jpg" || extension == "gif" || extension == "jpeg">
                                <div class="grid-item">
                                    <a data-fslightbox="gal" href="/api/file/download?path=${filename}">
                                        <img src="/api/file/download?path=${filename}" alt="image">
                                    </a>
                                </div>

                            <#elseif extension == "mp4" || extension == "avi" || extension == "mov">
                                <div class="grid-item video-container">
                                    <a data-fslightbox="gal" href="/api/file/download?path=${filename}">
                                        <video src="/api/file/download?path=${filename}"></video>
                                    </a>
                                </div>
                            </#if>
                        </#list>
                    </div>
                </div>
            </div>
        </div>
        <#if !appeal.status??>
            <form action="/appeals/approve" method="post" id="appealForm">
                <div class="card my-3 border-0 rounded-4 shadow-sm">
                    <div class="card-body">
                        <input type="hidden" name="appealId" value="${appeal.id}">
                        <div class="form-group my-2">
                            <label for="exampleFormControlTextarea1">Ваш ответ: </label>
                            <textarea class="form-control" name="respond" id="exampleFormControlTextarea1"
                                      rows="2"></textarea>
                        </div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="status" id="inlineRadio1" value="true">
                            <label class="form-check-label" for="inlineRadio1">Подтвердить</label>
                        </div>
                        <div class="form-check form-check-inline">
                            <input class="form-check-input" type="radio" name="status" id="inlineRadio2" value="false">
                            <label class="form-check-label" for="inlineRadio2">Отклонить</label>
                        </div>
                        <p class="card-text text-danger " id="error" style="display: none;">Пожалуйста, выберите
                            Подтвердить или Отклонить. </p>
                    </div>
                </div>
                <div class="d-flex justify-content-end">
                    <button class="btn btn-dodo rounded-4 w-50 h-100" id="send" type="submit">Отправить</button>
                </div>
            </form>
        </#if>


    </div>

    <script src="/js/fslightbox.js"></script>


    <script>

        document.getElementById('appealForm').addEventListener('submit', function (event) {
            let radio1 = document.getElementById('inlineRadio1');
            let radio2 = document.getElementById('inlineRadio2');

            if (!radio1.checked && !radio2.checked) {
                let error = document.getElementById('error').style.display = 'block';
                event.preventDefault();
            }
        });
    </script>
</@main.layout>