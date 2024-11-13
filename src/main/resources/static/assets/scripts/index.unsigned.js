const $cover = document.getElementById('cover');
const $main = document.getElementById('main');
const $registerForm = document.getElementById('registerForm')
{
    const $content = $main.querySelector(':scope > .content')
    const $loginForm = $content.querySelector(':scope > .login-form')
    const $menu = $loginForm.querySelector(':scope > .menu')
    $menu.querySelector(':scope > .item > [rel="register"]').onclick = (e) => {
        e.preventDefault();
        $cover.onclick = () => { // 커버 ( 바깥쪽 ) 누르면 빠져나와야 하니까
            $cover.hide();
            $registerForm.hide();
        };
        $cover.show();
        $registerForm.reset(); // 필드 초기화
        $registerForm.show();
    };
}

window.onload = () => {
    const $content = $main.querySelector(':scope > .content');
    const $logo = $content.querySelector(':scope > .logo');
    const $loginForm = $content.querySelector(':scope > .login-form');
    setTimeout(() => $logo.show(), 100);
    setTimeout(() => $loginForm.show(), 1000);


};

