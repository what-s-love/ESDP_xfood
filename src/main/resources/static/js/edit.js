document.addEventListener('DOMContentLoaded', () => {
    let values = document.querySelectorAll('[id^="value-"]');
    let sum = document.getElementById('sum');
    let totalSum = 0;


    initTotalSum();


    async function initTotalSum() {
        totalSum = 0;
        let idsAfterDash = [];
        values.forEach((input) => {
            let idParts = input.id.split('-');
            let idAfterDash = idParts[1];
            idsAfterDash.push(idAfterDash);
        });

        for (const element of idsAfterDash) {
            let criteria = await getCriteriaByCheckAndCriteriaId(element);
            if (criteria.section !== "Критический фактор" && criteria.section !== "WOW фактор") {
                totalSum += parseInt(criteria.maxValue) || 0;
            }
        }

        updateTotalSum();
    }


    async function updateTotalSum() {
        let maxValueInputs = document.querySelectorAll('[id^="value-"]');
        let currentSum = await countTotalSum(maxValueInputs);
        sum.innerHTML = `<p>${currentSum}/${totalSum}</p>`;
    }

    async function countTotalSum(maxValueInputs) {
        let summary = 0;
        let idsAfterDash = [];
        maxValueInputs.forEach((input) => {
            let idParts = input.id.split('-');
            let idAfterDash = idParts[1];
            idsAfterDash.push(idAfterDash);
        });

        for (const element of idsAfterDash) {
            let criteria = await getCriteriaByCheckAndCriteriaId(element);
            if (criteria.section !== "Критический фактор" && criteria.section !== "WOW фактор") {
                let inputValue = document.getElementById("value-"+criteria.id).value;
                summary += parseInt(inputValue) || 0;
            }
        }

        return summary;
    }

    values.forEach((element) => {
        element.addEventListener('input', () => {
            updateTotalSum();
        });
    });


    let search = document.getElementById('searchbar');
    search.addEventListener('input', onInput);

    function onInput(event) {
        event.preventDefault();
        let input = document.getElementById('searchbar');
        let description = input.value;
        let result = "/api/criteria/search?description=" + description;
        getResults(result);
    }

    async function getResults(result) {
        let response = await fetch(result);
        if (response.ok) {
            let json = await response.json();
            printResults(json);
        } else {
            console.log(response);
            alert("Ошибка HTTP: " + response.status);
        }
    }

    function printResults(json) {
        let objectSize = Object.keys(json).length;
        let searchResultsWrapper = document.querySelector('.found-criterion');
        searchResultsWrapper.innerHTML = "";
        if (objectSize > 0) {
            for (let i = 0; i < objectSize; i++) {
                let element = document.createElement('div');
                element.classList.add('card');
                element.id = 'del-' + json[i].id;
                element.innerHTML =
                    '<div class="card-body">' +
                    '<h5 class="card-title">' + json[i].section + '</h5>' +
                    '<h6 class="card-subtitle mb-2 text-muted">' + json[i].zone + '</h6>' +
                    '<p class="card-text">' + json[i].description + '</p>' +
                    '<button class="btn btn-link" type="button" id="add-criteria-' + json[i].id + '" data-id="' + json[i].id + '">' +
                    '<i class="bi bi-plus-circle-fill text-success fs-3"></i>' +
                    '</button>' +
                    '</div>';
                searchResultsWrapper.appendChild(element);
            }

            let addCriteriaButtons = document.querySelectorAll('[id^="add-criteria-"]');
            for (let btn of addCriteriaButtons) {
                btn.addEventListener('click', function () {
                    let id = this.getAttribute('data-id');
                    addCriteriaToList(id);
                });
            }
        } else {
            let noResultsSection = document.createElement('div');
            noResultsSection.innerText = 'не найдено';
            searchResultsWrapper.appendChild(noResultsSection);
        }
    }

    async function addCriteriaToList(id) {
        let del = document.getElementById('del-' + id);
        if (del) del.remove();

        let criteriaList = document.querySelector('.criterion-list');
        let criteriaWrap = document.getElementById('criteria-wrap-' + id);
        if (!criteriaWrap) {
            let criteria = await getCriteriaById(id);
            let newCriteria = document.createElement('tr');
            newCriteria.setAttribute('id', 'criteria-wrap-' + id);
            newCriteria.innerHTML =
                `<th scope="row">${criteria.section}</th>
             <input type="hidden" value="${criteria.id}" name="criterion[${id}].id">
             <td>${criteria.zone}</td>
             <td>${criteria.description}</td>
             <td>
                <input type="number" name="criterion[${id}].maxValue"  class="form-control form-control-sm" required ${criteria.section === '' ? `min="1" value="1"` : `value="${criteria.coefficient}" readonly`} id="value-${id}">
             </td>
             <td>
                <button class="btn btn-link bg-white shadow-sm rounded-4 p-2" type="button" id="deleteCriteria-${id}">
                    <i class="bi bi-trash text-secondary fs-4"></i>
                </button>
             </td>`;
            criteriaList.appendChild(newCriteria);


            let deleteButton = document.getElementById('deleteCriteria-' + id);
            deleteButton.addEventListener('click', function () {
                document.getElementById('criteria-wrap-' + id).remove();
                updateTotalSum();
            });

            setupMaxValueInputs();
        }
    }

    async function getCriteriaById(id) {
        let response = await fetch("/api/criteria/get/" + id);
        if (response.ok) {
            return await response.json();
        } else {
            console.log(response);
            alert("Ошибка HTTP: " + response.status);
        }
    }

    async function getCriteriaByCheckAndCriteriaId(id) {
        let checkId = document.getElementById('checkId').value;
        let response = await fetch("/api/criteria/find/" + checkId + "/" + id);
        if (response.ok) {
            return await response.json();
        } else {
            console.log(response);
            alert("Ошибка HTTP: " + response.status);
        }
    }

    let saveButton = document.getElementById('form');
    saveButton.addEventListener('submit', (e) => {
        validate(e);
    });

    async function validate(event) {
        event.preventDefault();
        let list = document.getElementById('criterion-list');
        let zone = document.getElementById('zoneSelect').value;
        let section = document.getElementById('sectionSelect').value;
        let description = document.getElementById('descriptionInput').value;
        let coefficient = document.getElementById('coefficientInput').value;

        const data = {
            description: description,
            coefficient: coefficient,
            zone: zone,
            section: section
        };
        console.log(data);
        try {
            const resp = await fetch("/api/criteria/create", {
                method: "POST",
                mode: "cors",
                cache: "no-cache",
                credentials: "same-origin",
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            });

            const respData = await resp.json();

            if (!resp.ok) {
                console.log(respData);
                handleErrors(respData);
            } else {
                const createdId = respData;
                console.log("new criteria id " + createdId);
                let criteriaList = document.querySelector('.criterion-list');
                let newCriteria = document.createElement('tr');
                newCriteria.setAttribute('id', 'criteria-wrap-' + createdId);
                newCriteria.innerHTML =
                    '<th scope="row">' + data.section + '</th>' +
                    '<input type="hidden" value="' + createdId + '" name="criterion[' + createdId + '].id">' +
                    '<td>' + data.zone + '</td>' +
                    '<td>' + data.description + '</td>' +
                    '<td>' +
                    '<input type="number" name="criterion[' + createdId + '].maxValue"  class="form-control form-control-sm" required  ' + (data.section === '' ? 'min="1" value="1"' : 'readonly value="' + data.coefficient + '"') + ' id="value-' + createdId + '">' +
                    '</td>' +
                    '<td>' +
                    '<button class="btn btn-link bg-white shadow-sm rounded-4 p-2" type="button" id="deleteCriteria-' + createdId + '">' +
                    '<i class="bi bi-trash text-secondary fs-4"></i>' +
                    '</button>' +
                    '</td>';
                criteriaList.appendChild(newCriteria);

                let deleteButton = document.getElementById('deleteCriteria-' + createdId);
                deleteButton.addEventListener('click', function () {
                    document.getElementById('criteria-wrap-' + createdId).remove();
                    updateTotalSum();
                });
                setupMaxValueInputs();
                let modal = document.getElementById('create');
                modal.classList.remove('show');
                modal.style.display = 'none';
                document.body.classList.remove('modal-open');
                let modalBackdrop = document.getElementsByClassName('modal-backdrop')[0];
                if (modalBackdrop) {
                    modalBackdrop.parentNode.removeChild(modalBackdrop);
                }
            }
        } catch (err) {
            console.log(err);
        }
    }
    function setupMaxValueInputs() {
        let maxValueInputs = document.querySelectorAll('[id^="value-"]');
        for (let input of maxValueInputs) {
            input.addEventListener('input', () => {
                updateTotalSum();
            });
        }
        updateTotalSum();
    }


    function handleErrors(errorData) {
        let desc = document.getElementById('descr-err');
        let coeff = document.getElementById('coef-err');
        let zoneER = document.getElementById('zone-err');
        let sectionER = document.getElementById('section-err');
        desc.innerHTML = "";
        coeff.innerHTML = "";
        zoneER.innerHTML = "";
        sectionER.innerHTML = "";

        for (const [field, message] of Object.entries(errorData)) {
            const errorElement = document.createElement("p");
            if (field === 'description') {
                errorElement.textContent = message + '';
                desc.appendChild(errorElement)
            } else if (field === 'section') {
                sectionER.textContent = message + '';
                desc.appendChild(errorElement)
            } else {
                errorElement.textContent = message + '';
                coeff.appendChild(errorElement);
            }
        }
    }
    let sectionSelect = document.getElementById('sectionSelect');
    let zoneSelect = document.getElementById('zoneSelect');
    let coefficientInput = document.getElementById('coefficientInput');
    let descriptionInput = document.getElementById('descriptionInput');
    let coefLabel = document.getElementById('coef-label');
    let zoneLabel = document.getElementById('zoneLabel')

    sectionSelect.addEventListener('change', function () {
        toggleFields(sectionSelect.value);
    });
    function toggleFields(value) {
        if (value === '') {
            coefficientInput.style.display = 'none';
            coefLabel.style.display = 'none';
            zoneLabel.style.display = 'block';
            zoneSelect.style.display = 'block';
        } else if (value === 'Критический фактор') {
            zoneLabel.style.display = 'none';
            zoneSelect.style.display = 'none';
            coefLabel.style.display = 'block';
            coefficientInput.style.display = 'block';
            coefficientInput.min = '';
            coefficientInput.max = -1;
            coefficientInput.value = -1;
        } else if (value === 'WOW фактор') {
            zoneLabel.style.display = 'none';
            zoneSelect.style.display = 'none';
            coefLabel.style.display = 'block';
            coefficientInput.style.display = 'block';
            coefficientInput.min = 1;
            coefficientInput.max = '';
            coefficientInput.value = 1;
        }
    }


    document.getElementById('create').addEventListener('shown.bs.modal', function () {
        toggleFields(sectionSelect.value);
    });
    document.getElementById('create').addEventListener('hidden.bs.modal', function () {
        clearFields();
    });
    function clearFields() {
        descriptionInput.value = '';
        coefficientInput.value = '';
        toggleFields(sectionSelect.value);
    }

    let criteriaWraps = document.querySelectorAll('[id^="criteria-wrap-"]');
    let criterias = document.querySelectorAll('[id^="deleteCriteria"]');
    for (let i = 0; i < criterias.length; i++) {
        criterias[i].addEventListener('click', () => {
            criteriaWraps[i].remove();
            setupMaxValueInputs();
            updateTotalSum();
        });
    }


    updateTotalSum();
    setupMaxValueInputs();

    let edit = document.getElementById('edit');
    edit.addEventListener("submit", ev => {
        ev.preventDefault()
        checkSum();
    });

    async function checkSum(){
        let sum = 0;
        let valueInputs = document.querySelectorAll('[id^="value-"]');
        let idsAfterDash = [];
        valueInputs.forEach((input) => {
            let idParts = input.id.split('-');
            let idAfterDash = idParts[1];
            idsAfterDash.push(idAfterDash);
        });
        for (const element of idsAfterDash) {
            let criteria = await getCriteriaByCheckAndCriteriaId(element);
            if (criteria.section !== "Критический фактор" && criteria.section !== "WOW фактор") {
                let inputValue = document.getElementById("value-"+criteria.id).value;
                sum += parseInt(inputValue) || 0;
            }
        }
        console.log("total "+totalSum +" sum "+sum)
        if (sum < totalSum || sum>totalSum){
            document.getElementById('error').style.display = 'block';

        }else {
            edit.submit();
        }
    }

});