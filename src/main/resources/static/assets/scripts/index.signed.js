const $nav = document.getElementById('nav');
const $navItems = Array.from($nav.querySelectorAll(':scope > .menu > .item[rel]'));
// querySelectorAll 은 유사배열 객체라서 Array를 사용함
const $main = document.getElementById('main');
const $mainContents = Array.from($main.querySelectorAll(':scope > .content[rel]'));

$navItems.forEach(($navItem) => {
    $navItem.onclick = () => {
        // 여기까지 작성 했으면 alert($navItem.innerText); 로 찍어서 onclick 이 먹히는지 확인 하자

        const rel = $navItem.getAttribute('rel'); // mymusic.register
        const $mainContent = $mainContents.find((x) => x.getAttribute('rel') === rel);
        $navItems.forEach((x) => x.classList.remove('-selected'));
        $navItem.classList.add('-selected');
        $mainContents.forEach((x) => x.hide());
        $mainContent.show();
    };
});

{
    const $content = $mainContents.find((x) => x.getAttribute('rel') === 'mymusic.register');
    const $form = $content.querySelector(':scope > form');
    $form['youtubeIdCheckButton'].onclick = () => {
        // 이쯤에서 alert 써서 연결 상태 확인하기
        const youtubeId = $form['youtubeId'].value;
        if (youtubeId.length !== 11) {
            Dialog.show({
                title: '유튜브 식별자 확인',
                content: '올바른 유튜브 식별자를 입력해 주세요',
                buttons: [{
                    text: '확인', onclick: ($dialog) => {
                        Dialog.hide($dialog);
                        $form['youtubeId'].focus();
                        $form['youtubeId'].select();
                    }
                }]
            });
            return;
        }
        const $text = $form.querySelector(':scope > .youtube > .row > .iframe-wrapper > .text');
        const $iframe = $form.querySelector(':scope > .youtube > .row > .iframe-wrapper > .iframe');

        const xhr = new XMLHttpRequest();

        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) {
                return;
            }
            Loading.hide();
            if (xhr.status === 404) {
                Dialog.show({
                    title: '유튜브 식별자 확인',
                    content: `입력하신 식별자 <b>${youtubeId}</b>로 조회할 수 있는 영상이 확인되지 않습니다. <br><br>식별자를 다시 확인해 주세요.`,
                    buttons: [{
                        text: '확인', onclick: ($dialog) => {
                            Dialog.hide($dialog);
                            $form['youtubeId'].focus();
                            $form['youtubeId'].select();
                        }
                    }]
                });
                return;
            }

            const response = JSON.parse(xhr.responseText);
            if (response['result'] === true) {
                $iframe.src = `https://www.youtube.com/embed/${youtubeId}`;
                $iframe.style.display = 'block';
                $text.style.display = 'none';
            }
            else {
                Dialog.show({
                    title: '유튜브 식별자 확인',
                    content: `입력하신 식별자 <b>${youtubeId}</b>로 조회할 수 있는 영상이 확인되지 않습니다. <br><br>식별자를 다시 확인해 주세요.`,
                    buttons: [{
                        text: '확인', onclick: ($dialog) => {
                            Dialog.hide($dialog);
                            $form['youtubeId'].focus();
                            $form['youtubeId'].select();
                        }
                    }]
                });
            }
        };
        xhr.open('GET', `/music/verify-youtube-id?id=${youtubeId}`);
        xhr.send();
        Loading.show(0);
        $iframe.style.display = 'none';
        $text.style.display = 'flex';
    };
}