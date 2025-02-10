
const url ='/api/admin'

async function newUser() {
    try {
        const response = await fetch(url+'/roles')
        const roles = await response.json()
        roles.forEach(role => {
            let element = document.createElement('option')
            element.text = role.name.substring(5);
            element.value = role.id
            $('#rolesNewUser')[0].appendChild(element)
        })
        const formAddNewUser = document.forms['formAddNewUser']
        formAddNewUser.addEventListener('submit', function (event) {
            event.preventDefault()
            let rolesNewUser = []
            for (let i = 0; i < formAddNewUser.roles.options.length; i++) {
                if (formAddNewUser.roles.options[i].selected) {
                    rolesNewUser.push({
                        id: formAddNewUser.roles.options[i].value,
                        name: formAddNewUser.roles.options[i].text
                    })
                    break
                }
            }
            fetch(url+'/create', {
                method: 'POST',
                headers: {'Content-Type': 'application/json'},
                body: JSON.stringify({
                    username: formAddNewUser.username.value,
                    email: formAddNewUser.email.value,
                    password: formAddNewUser.password.value,
                    roles: rolesNewUser
                })
            }).then(() => {
                formAddNewUser.reset()
                // window.location.assign("http://localhost:8080/admin");
                $('#home-tab').click();
                allUsers()
            })
        })
    } catch(e) {
        console.error(e)
    }
}