<#import "../layout.ftlh" as main>
<@main.layout>
    <#assign known = SPRING_SECURITY_CONTEXT??>
    <#if known>
        <#assign authorizedUser = SPRING_SECURITY_CONTEXT.authentication>
    </#if>


    <style>
        .user-info {
            flex-grow: 1;
        }

        .card-title {
            font-size: 1.25rem;
            font-weight: 600;
        }

        .card-text {
            margin-bottom: 5px;
        }

        .card-container {
            margin-bottom: 20px;
        }

        .btn-edit, .btn-delete {
            border: none;
            background: none;
            color: #007bff;
            cursor: pointer;
            font-size: 1.25rem;
            padding: 0 5px;
        }

        .btn-edit:hover, .btn-delete:hover {
            color: #0056b3;
        }

        .pagination .page-item.active .page-link {
            background-color: #007bff;
            border-color: #007bff;
        }

        .modal-header, .modal-footer {
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
    </style>

    <div class="container">
        <div class="row row-cols-md-3 row-gap-3">
            <div class="col-md-4 col-xl-3 bg-body sidebar">
                <div class="row row-cols-1 row-gap-3">
                    <div class="col">
                        <div class="card rounded-4 mb-2 mb-sm-0 shadow-sm border-0">
                            <div class="card-body">
                                <div class="row row-cols-1 row-gap-3 h-100">
                                    <div class="col">
                                        <div class="row row-cols-4 row-cols-md-1 row-gap-2">
                                            <div class="col">
                                                <a href="/deadlines" class="btn rounded-4 w-100 h-100 btn-dodo-light">
                                                    <i class="bi bi-hourglass-split fs-5 d-md-none"></i>
                                                    <span class="d-none d-md-inline">
                                                        Дедлайны
                                                    </span>
                                                </a>
                                            </div>
                                            <div class="col">
                                                <a class="btn rounded-4 w-100 h-100 btn-dodo-light"
                                                   data-bs-toggle="modal" data-bs-target="#tokenModal">
                                                    <i class="bi bi-key fs-5 d-md-none"></i>
                                                    <span class="d-none d-md-inline">
                                                        Токены
                                                    </span>
                                                </a>
                                            </div>
                                            <div class="col">
                                                <a href="/templates" class="btn rounded-4 w-100 h-100 btn-dodo-light">
                                                    <i class="bi bi-clipboard-plus fs-5 d-md-none"></i>
                                                    <span class="d-none d-md-inline">
                                                        Шаблоны
                                                    </span>
                                                </a>
                                            </div>
                                            <div class="col">
                                                <a href="#" class="btn disabled rounded-4 w-100 h-100 btn-dodo-light">
                                                    <span class="d-none d-md-inline">
                                                        Пользователи
                                                    </span>
                                                    <i class="bi bi-people d-md-none fs-5"></i>
                                                </a>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                    <div class="col">
                        <div class="card d-none d-md-block border-0 shadow-sm rounded-4 ">
                            <div class="card-body">
                                <h6>Фильтры</h6>
                                <form action="" method="GET">
                                    <div class="mb-3">
                                        <label for="role-select" class="form-label">Тип пользователя</label>
                                        <select class="form-select" id="role-select-1" name="role">
                                            <option value="default">Все</option>
                                        </select>
                                    </div>
                                    <div class="input-group mb-3">
                                        <input type="text" name="search" value="<#if searchWord??>${searchWord}</#if>"
                                               class="form-control" placeholder="Поиск">
                                    </div>
                                    <button class="btn btn-green rounded-4 w-100" type="submit">Применить
                                    </button>
                                </form>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="card-container col-xl-9 col-md-8" id="user-list">
                <#if users?size == 0>
                    <div class="alert alert-warning" role="alert">
                        Ничего не найдено.
                    </div>
                <#else>
                    <div class="d-flex flex-row gap-2 mb-3 flex-wrap">
                        <div class="dropdown d-md-none">
                            <button class="btn btn btn-light rounded-4 dropdown-toggle" type="button"
                                    data-bs-toggle="dropdown" aria-expanded="false">
                                <i class="bi bi-funnel"></i> Фильтр
                            </button>
                            <div class="dropdown-menu p-3 rounded-4 shadow border-0 w-100">
                                <h6>Фильтры</h6>
                                <form action="/users" method="GET">
                                    <div class="mb-3">
                                        <label for="role-select" class="form-label">Тип пользователя</label>
                                        <select class="form-select" id="role-select-2" name="role">
                                            <option value="default">Все</option>
                                        </select>
                                    </div>
                                    <div class="input-group mb-3">
                                        <input type="text" name="search" value="<#if searchWord??>${searchWord}</#if>"
                                               class="form-control" placeholder="Поиск">
                                    </div>
                                    <button class="btn btn-green rounded-4 w-100" type="submit">Применить
                                    </button>
                                </form>
                            </div>
                        </div>
                        <button class="btn btn-dodo rounded-4" data-bs-toggle="modal" data-bs-target="#addUser">
                            <i class="bi bi-person-fill-add"></i> Создать <span
                                    class="d-none d-sm-inline">пользователя</span>
                        </button>
                    </div>
                    <#list users as user>
                        <div class="card border-0 shadow-sm rounded-4 mb-2 user-card">
                            <div class="card-body d-flex">
                                <div class="user-info">
                                    <h5 class="card-title">ФИО: ${user.getName()} ${user.getSurname()}</h5>
                                    <p class="card-text">Роль: ${user.getRole()}</p>
                                    <p class="card-text">Почта: ${user.getEmail()}</p>
                                    <p class="card-text">Телеграм: ${user.getTgLink()}</p>
                                    <p class="card-text">Активный: <#if user.getEnabled() == true>Да<#else>Нет</#if></p>
                                </div>
                                <div class="card-icons">
                                    <#if currentUser.role == "SUPERVISOR" && (user.role == "ADMIN" || user.role == "SUPERVISOR") && currentUser != user>
                                    <#elseif currentUser.role == "ADMIN" && user.role == "ADMIN">
                                    <#elseif currentUser == user>
                                        <button class="btn-edit" data-user-id="${user.getId()}"><i
                                                    class="fas fa-edit"></i></button>
                                    <#else>
                                        <button class="btn-edit" data-user-id="${user.getId()}"><i
                                                    class="fas fa-edit"></i></button>
                                        <button class="btn-delete" data-user-id="${user.getId()}"><i
                                                    class="fas fa-trash-alt"></i>
                                        </button>
                                    </#if>
                                </div>
                            </div>
                        </div>
                    </#list>
                </#if>

                <nav aria-label="Page navigation example">
                    <ul class="pagination justify-content-center">
                        <#if totalPages gt 0>
                            <#if currentPage gt 0>
                                <li class="page-item">
                                    <a class="page-link"
                                       href="/users?role=${currentRole}&page=${currentPage-1}&size=${currentSize}&search=${searchWord}"
                                       aria-label="Previous">
                                        <span aria-hidden="true">&laquo;</span>
                                        <span class="sr-only">Previous</span>
                                    </a>
                                </li>
                            <#else>
                                <li class="page-item disabled">
                                    <a class="page-link" href="#" aria-label="Previous">
                                        <span aria-hidden="true">&laquo;</span>
                                        <span class="sr-only">Previous</span>
                                    </a>
                                </li>
                            </#if>

                            <#list 0..(totalPages-1) as page>
                                <li class="page-item <#if page == currentPage>active</#if>">
                                    <a class="page-link"
                                       href="/users?role=${currentRole}&page=${page}&size=${currentSize}&search=${searchWord}">${page + 1}</a>
                                </li>
                            </#list>

                            <#if currentPage lt totalPages - 1>
                                <li class="page-item">
                                    <a class="page-link"
                                       href="/users?role=${currentRole}&page=${currentPage+1}&size=${currentSize}&search=${searchWord}"
                                       aria-label="Next">
                                        <span aria-hidden="true">&raquo;</span>
                                        <span class="sr-only">Next</span>
                                    </a>
                                </li>
                            <#else>
                                <li class="page-item disabled">
                                    <a class="page-link" href="#" aria-label="Next">
                                        <span aria-hidden="true">&raquo;</span>
                                        <span class="sr-only">Next</span>
                                    </a>
                                </li>
                            </#if>
                        </#if>
                    </ul>
                </nav>
            </div>
        </div>
    </div>

    <div class="modal fade" id="editUserModal" tabindex="-1" aria-labelledby="editUserModalLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content rounded-4">
                <div class="modal-header">
                    <h5 class="modal-title" id="editUserModalLabel">Редактировать пользователя</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <form id="editUserForm">
                        <div class="mb-3">
                            <label for="editName" class="form-label">Имя</label>
                            <input type="text" class="form-control" id="editName" name="name">
                        </div>
                        <div class="mb-3">
                            <label for="editSurname" class="form-label">Фамилия</label>
                            <input type="text" class="form-control" id="editSurname" name="surname">
                        </div>
                        <div class="mb-3">
                            <label for="editRole" class="form-label">Роль</label>
                            <select class="form-select" id="editRole" name="role">

                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="editEmail" class="form-label">Почта</label>
                            <input type="email" disabled class="form-control" id="editEmail" name="email">
                        </div>
                        <div class="mb-3">
                            <label for="editPhoneNumber" class="form-label">Телеграм</label>
                            <input type="text" disabled class="form-control" id="editPhoneNumber" name="tgLink">
                        </div>
                        <div class="mb-3" id="edit-user-enabled">
                            <label for="editEnabled" class="form-label">Активный</label>
                            <select class="form-select" id="editEnabled" name="enabled">
                                <option value="true">Да</option>
                                <option value="false">Нет</option>
                            </select>
                        </div>
                        <input type="hidden" id="editUserId">

                    </form>
                </div>
                <div class="modal-footer border-0">
                    <button type="button" class="btn btn-link link-secondary text-decoration-none rounded-4"
                            data-bs-dismiss="modal">
                        Закрыть
                    </button>
                    <button type="submit" class="btn btn-dodo-light rounded-4" id="saveChanges">Сохранить изменения</button>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade modal-lg" id="addUser" tabindex="-1" aria-labelledby="addUserLabel" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content rounded-4">

                <div class="modal-header">
                    <h5 class="modal-title" id="addUserLabel">Введите данные нового сотрудника</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>

                <div class="modal-body">
                    <form id="addUserForm" class="needs-validation" novalidate>
                        <div class="mb-4">
                            <label for="name" class="form-label">Имя</label>
                            <input type="text" id="name" name="name" class="form-control" required>
                        </div>

                        <div class="mb-4">
                            <label for="surname" class="form-label">Фамилия</label>
                            <input type="text" id="surname" name="surname" class="form-control" required>
                        </div>

                        <div class="mb-4">
                            <label class="form-label" for="tgLink">Ccылка на телеграм</label>
                            <div class="input-group">
                                <div class="input-group-text">
                                    t.me/
                                </div>
                                <input type="text" id="tgLink" name="tgLink" class="form-control" required
                                       pattern="^t\.me/.*"
                                       placeholder="Имя пользователя без @"/>
                            </div>
                        </div>

                        <div class="mb-4">
                            <label for="email" class="form-label">Адрес эл. почты</label>
                            <input type="email" id="email" name="email" class="form-control" required>
                            <div id="emailError" class="invalid-feedback"></div>
                        </div>

                        <div class="mb-4">
                            <label for="password" class="form-label">Пароль</label>
                            <input type="password" id="password" name="password" class="form-control" required
                                   pattern="^(?=.*\d)(?=.*[a-zA-Z]).{8,}$"
                                   placeholder="Не менее 8 симв., цифры и буквы."/>
                        </div>

                        <#if authorizedUser??>
                            <#if authorizedUser.authorities[0].authority??>
                                <#if authorizedUser.authorities[0].authority == "ROLE_ADMIN" >
                                    <div class="mb-4">
                                        <div class="row row-cols-2 gap-2">
                                            <div class="col-auto">
                                                <p class="mb-1">Выберите роль:</p>
                                            </div>
                                            <div class="col-auto">
                                                <div class="d-flex flex-wrap gap-2">
                                                    <div class="form-check">
                                                        <input type="radio" id="expert" name="role" value="expert"
                                                               class="form-check-input"
                                                               checked required>
                                                        <label class="form-check-label" for="expert">Эксперт</label>
                                                    </div>
                                                    <div class="form-check">
                                                        <input type="radio" id="supervisor" name="role"
                                                               value="supervisor"
                                                               class="form-check-input" required>
                                                        <label class="form-check-label"
                                                               for="supervisor">Руководитель</label>
                                                    </div>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                <#else>
                                    <div class="mb-4">
                                        <div class="d-flex ">
                                            <p class="me-3">Выберите роль:</p>
                                            <div class="form-check me-3">
                                                <input type="radio" id="expert" name="role" value="expert"
                                                       class="form-check-input"
                                                       checked required>
                                                <label class="form-check-label" for="expert">Эксперт</label>
                                            </div>
                                        </div>
                                    </div>
                                </#if>
                            <#else>
                            </#if>
                        <#else>
                        </#if>

                        <div class="d-flex justify-content-end">
                            <button type="submit" class="btn btn-dodo rounded-4">Создать</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="tokenModal" tabindex="-1" aria-labelledby="tokenModal"
         aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content rounded-4">
                <div class="modal-header">
                    <h5 class="modal-title" id="tokenModal">
                        Привязать токен доступа
                    </h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal"
                            aria-label="Close"></button>
                </div>
                <form id="bearerForm">
                    <div class="modal-body">
                        <label for="bearer" class="form-label">Токен</label>
                        <input type="text" id="bearer" name="bearer" class="form-control" required>
                    </div>

                    <div class="modal-footer border-0">
                        <button type="submit" id="pushToken" class="btn btn-dodo rounded-4" data-bs-dismiss="modal"
                                data-bs-target="#tokenModal">
                            Сохранить токен
                        </button>
                    </div>
                </form>

            </div>
        </div>
    </div>

    <script>
        document.getElementById('pushToken').addEventListener('click', async function () {
            const form = document.getElementById('bearerForm');
            const formData = new FormData(form);

            try {
                const response = await fetch('/api/bearer', {
                    method: 'POST',
                    body: formData
                });

                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }

                // Optionally, handle the response here
                const result = await response.json();
                console.log(result);

                // Close the modal if needed
                const modal = new bootstrap.Modal(document.getElementById('exampleModal'));
                modal.hide();
            } catch (error) {
                console.error('There was a problem with the fetch operation:', error);
            }
        });
    </script>
    <script src="/js/validation.js"></script>
    <script>
        document.addEventListener('DOMContentLoaded', async () => {
            const roleSelect1 = document.getElementById('role-select-1');
            const roleSelect2 = document.getElementById('role-select-2');
            const editUserModal = new bootstrap.Modal(document.getElementById('editUserModal'));

            async function getRoles(select, currentRole) {
                const response = await fetch('/api/role');
                const data = await response.json();
                data.forEach(function (role) {
                    let option = document.createElement('option');
                    option.value = role;
                    option.textContent = role;
                    if (currentRole === role) {
                        option.selected = true;
                    }
                    select.appendChild(option);
                });
            }

            document.addEventListener('click', async (event) => {
                if (event.target.closest('.btn-delete')) {
                    const btnDelete = event.target.closest('.btn-delete');
                    const userId = btnDelete.getAttribute('data-user-id');
                    await deleteUser(userId);
                }

                if (event.target.closest('.btn-edit')) {
                    editUserModal.show()
                    const btnEdit = event.target.closest('.btn-edit');
                    const userId = btnEdit.getAttribute('data-user-id');
                    editUser(userId);
                }
            });

            async function deleteUser(userId) {
                await fetch('/api/admin/user/' + userId, {
                    method: 'DELETE'
                })
                location.reload();
            }

            async function editUser(userId) {
                const response = await fetch('/api/admin/user/' + userId);
                const user = await response.json();

                document.getElementById('editUserId').value = userId;
                document.getElementById('editName').value = user.name;
                document.getElementById('editSurname').value = user.surname;
                await getRoles(document.getElementById('editRole'), user.role);
                document.getElementById('editEmail').value = user.email;
                document.getElementById('editPhoneNumber').value = user.tgLink;
                document.getElementById('editEnabled').value = user.enabled;

                const editEnabledField = document.getElementById('editEnabled');

                if (userId === '${currentUser.id}') {
                    editEnabledField.disabled = true;
                    editEnabledField.classList.add('d-none');
                } else {
                    editEnabledField.disabled = false;
                    editEnabledField.classList.remove('d-none');
                }

                editUserModal.show();
            }


            document.getElementById('editUserForm').addEventListener('submit', async (event) => {
                event.preventDefault();
                const userId = document.getElementById('editUserId').value;
                const userEditDto = {
                    id: userId,
                    name: document.getElementById('editName').value,
                    surname: document.getElementById('editSurname').value,
                    enabled: document.getElementById('editEnabled').value,
                    role: document.getElementById('editRole').value
                };

                await fetch('/api/admin/user', {
                    method: 'put',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(userEditDto)
                });
                editUserModal.hide();
                location.reload();
            });


            await getRoles(roleSelect1, '${currentRole}');
            await getRoles(roleSelect2, '${currentRole}')
        });
    </script>
</@main.layout>