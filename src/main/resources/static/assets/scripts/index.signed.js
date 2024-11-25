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

    const $melonResultInit = $form.querySelector(':scope > .melon > .row > .result > .init')
    const $melonResultLoading = $form.querySelector(':scope > .melon > .row > .result > .loading')
    const $melonResultEmpty = $form.querySelector(':scope > .melon > .row > .result > .empty')
    const $melonResultError = $form.querySelector(':scope > .melon > .row > .result > .error')
    let melonSearchTimeout;
    let melonSearchLastKeyword; // 여기에 입력한 값을 넣겠다 <-- 이게 1초 뒤에 실행됨
    // 딜레이 걸어주는거임
    $form['melonKeyword'].addEventListener('keyup', () => {
        $form.querySelectorAll(':scope > .melon > .row > .result > .item').forEach((x) => x.remove()); // 기존 검색 결과 삭제
        // keydown -> input -> keyup === input이 들어간 상태
        $melonResultEmpty.style.display = 'none';
        $melonResultError.style.display = 'none';
        if ($form['melonKeyword'].value === '') {
            $melonResultInit.style.display = 'flex';
            $melonResultLoading.style.display = 'none';
        }
        else {
            $melonResultInit.style.display = 'none';
            $melonResultLoading.style.display = 'flex';

            if (typeof melonSearchTimeout === 'number') {
                clearTimeout(melonSearchTimeout);
            }
            melonSearchLastKeyword = $form['melonKeyword'].value;
            melonSearchTimeout = setTimeout(() => {
            if (melonSearchLastKeyword !== $form['melonKeyword'].value) {
                return;
            }
            const xhr = new XMLHttpRequest();
            const url = new URL(location.href);
            url.pathname = '/music/search-melon';
            url.searchParams.set('keyword', $form['melonKeyword'].value);
            xhr.onreadystatechange = () => {
                if (xhr.readyState !== XMLHttpRequest.DONE ) {

                return;
                }
                $melonResultLoading.style.display = 'none';
                if (xhr.status < 200 || xhr.status >= 300 || xhr.responseText.length === 0) {
                    $melonResultError.style.display = 'flex';
                return;
                }
                const response = JSON.parse(xhr.responseText);
                if (response.length === 0) {
                    $melonResultEmpty.style.display = 'flex';
                }
                else {
                    const $result = $form.querySelector(':scope > .melon > .row > .result');
                    for (const music of response) {
                        const $item = document.createElement('span');
                        $item.classList.add('item');
                        const $image = document.createElement('img');
                        $image.classList.add('image');
                        $image.src = music['coverFileName'];
                        const $column = document.createElement('span');
                        $column.classList.add('column');
                        const $name = document.createElement('span');
                        $name.classList.add('name');
                        $name.innerText = music['name'];
                        const $artist = document.createElement('span');
                        $artist.classList.add('artist');
                        $artist.innerText = music['artist'];
                        $column.append($name, $artist);
                        $item.append($image, $column);
                        $item.onmousedown = () => {
                            $form['melonId'].value = music['youtubeId'];
                            $form['melonCrawlButton'].click();
                        };
                        $result.append($item);
                    }
                }
            };
            xhr.open('GET', url.toString());
            xhr.send();
            // 여기까지 왔으면 현재까지 적은 키워드 == 1초 뒤에 남아있는 키워드가 같다는것
            // XHR 요청 보냄
            }, 1000);
        }
    });

    $form['melonCrawlButton'].onclick = () => {
        const $melonLabel = $form.findLabel('melon');
        $melonLabel.setValid($form['melonId'].value.length > 0);
        if (!$melonLabel.isValid()) {
            return;
        }
        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE) {

                return;
            }
            Loading.hide();
            if (xhr.status < 200 || xhr.status >= 300) {
                Dialog.show({
                    title: '오류',
                    content: '요청을 전송하는 도중 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.',
                    buttons: [{
                        text: '확인',
                        onclick: ($dialog) => Dialog.hide($dialog)
                    }]
                });
                return;
            }
            if (xhr.responseText.length === 0) {
                Dialog.show({
                    title: '멜론 음원 검색',
                    content: `입력하신 멜론 음원 식별자 <b>${$form['melonId'].value}</b>를 통해 음원을 검색할 수 없습니다. <br><br>멜론 음원 식별자를 다시 확인해 주세요.`,
                    buttons: [{
                        text: '확인',
                        onclick: ($dialog) => Dialog.hide($dialog)
                    }]
                });
                return;
            }
            const response = JSON.parse(xhr.responseText);
            const $coverPreviewText = $form.querySelector(':scope > .cover > .row > .preview-wrapper > .text');
            const $coverPreviewImage = $form.querySelector(':scope > .cover > .row > .preview-wrapper > .image');
            $coverPreviewText.style.display = 'none';
            $coverPreviewImage.src = response['coverFileName'];
            $coverPreviewImage.style.display = 'block';
            if (response['youtubeId'] != null) {
                $form['youtubeId'].value = response['youtubeId'];
                $form['youtubeIdCheckButton'].click();
            }
            else {
                const $text = $form.querySelector(':scope > .youtube > .row > .iframe-wrapper > .text')
                const $iframe = $form.querySelector(':scope > .youtube > .row > .iframe-wrapper > .iframe')
                $text.style.display = 'flex'
                $iframe.style.display = 'none'
                $form['youtubeId'].value = '';
            }
            $form['artist'].value = response['artist'];
            $form['album'].value = response['album'];
            $form['releaseDate'].value = response['releaseDate'];
            $form['genre'].value = response['genre'];
            $form['name'].value = response['name'];
            $form['lyrics'].value = response['lyrics'];
        };

        xhr.open('GET', `/music/crawl-melon?id=${$form['melonId'].value}`);
        xhr.send();
        Loading.show(0);
    };
    $form['_cover'].onchange = () => {
        const $text = $form.querySelector(':scope > .cover > .row > .preview-wrapper > .text');
        const $image = $form.querySelector(':scope > .cover > .row > .preview-wrapper > .image');
        if (($form['_cover'].files?.length ?? 0) === 0) {
        // if ( $form['_cover].files == null || $form['_cover'].files.length === 0  와 같음
        $text.style.display = 'flex';
        $image.style.display = 'none';
        $text.src = '';
        return;
        }
        $text.style.display = 'none';
        $image.style.display = 'block';
        $image.src = URL.createObjectURL($form['_cover'].files[0]);
    };


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
            } else {
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

    $form.onsubmit = (e) => {
        // 오늘 새로 알게 된 것 : 그냥 ~.src는 src 가 비어있을때 현재 호스트( http://localhost:8080)를 돌려준다. 미친놈(Attribute('src') 는 정직하게 '' 를 줌
        e.preventDefault();
        const formData = new FormData();
        const $previewImage = $form.querySelector(':scope > .cover > .row > .preview-wrapper > .image');
        if ($previewImage.getAttribute('src').startsWith('http://') || $previewImage.getAttribute('src').startsWith('https://')) {
            // 멜론 이미지 사용한 것
            // MusicEntity::coverFileName 을 통해서 커버 이미지 URL 을 전달 받고 서버가 직접 다운로드 받아서
            // DB에 INSERT
            formData.append('coverFileName', $previewImage.getAttribute('src'));
        }
        else if ($previewImage.getAttribute('src').startsWith('blob:')) {
            // 이미지 직접 선택한 것
            // MultipartFile 로 전달 받아서 DB에 INSERT
            formData.append('_cover', $form['_cover'].files[0]);
        }
        else {
            // 이미지 선택 안 한 것
            Dialog.show({
                title: '음원 등록 신청',
                content: '커버 이미지를 선택해 주세요.',
                buttons: [{
                    text: '확인', onclick: ($dialog) => Dialog.hide($dialog)}]
            });
            return;
        }
        const $youtubeIframe = $form.querySelector(':scope > .youtube > .row > .iframe-wrapper > .iframe')
        if ($form['youtubeId'].value.length !== 11 || $form['youtubeId'].value !== $youtubeIframe.getAttribute('src').split('/').at(-1)) {
            Dialog.show({
                title: '음원 등록 신청',
                content: '유튜브 식별자를 검증해 주세요',
                buttons: [{
                    text: '확인', onclick: ($dialog) => Dialog.hide($dialog)}]
            });
            return;
        }
        formData.append('youtubeId', $form['youtubeId'].value);
        const $articleLabel = $form.findLabel('artist');
        const $albumLabel = $form.findLabel('album');
        const $releaseLabel = $form.findLabel('releaseDate');
        const $genreLabel = $form.findLabel('genre');
        const $nameLabel = $form.findLabel('name');
        $articleLabel.setValid($form['artist'].value.length >= 1 && $form['artist'].value.length <= 50);
        $albumLabel.setValid($form['album'].value.length >= 1 && $form['album'].value.length <= 50);
        $releaseLabel.setValid($form['releaseDate'].value !== '');
        $genreLabel.setValid($form['genre'].value.length >= 1 && $form['genre'].value.length <= 50);
        $nameLabel.setValid($form['name'].value.length >= 1 && $form['name'].value.length <= 50);
        if (!$articleLabel.isValid() || !$albumLabel.isValid() || !$releaseLabel.isValid() || !$genreLabel.isValid() || !$nameLabel.isValid) {
            return;
        }
        formData.append('artist', $form['artist'].value);
        formData.append('album', $form['album'].value);
        formData.append('releaseDate', $form['releaseDate'].value);
        formData.append('genre', $form['genre'].value);
        formData.append('name', $form['name'].value);
        formData.append('lyrics', $form['lyrics'].value);
        const xhr = new XMLHttpRequest();
        xhr.onreadystatechange = () => {
            if (xhr.readyState !== XMLHttpRequest.DONE ) {

            return;
            }
            Loading.hide();
            if (xhr.status < 200 || xhr.status >= 300 ) {
                Dialog.show({
                    title: '오류',
                    content: '요청을 전송하는 도중 오류가 발생하였습니다. 잠시 후 다시 시도해 주세요.',
                    buttons: [{
                        text: '확인',
                        onclick: ($dialog) => Dialog.hide($dialog)
                    }]
                });
            return;
            }
            const response = JSON.parse(xhr.responseText);
            const [title, content, onclick] = {
                failure: [`음원 등록 신청`, `알 수 없는 이유로 음원 등록 신청에 실패하였습니다. 잠시 후 다시 시도해 주세요 `, ($dialog) => Dialog.hide($dialog)],
                failure_duplicate_youtube_id: [`음원 등록 신청`, `입력하신 유튜브 식별자 <b>${$form['youtubeId'].value}</b>는 이미 등록되어 있습니다.<br><br>다시 한 번 확인해 주세요. `, ($dialog) => Dialog.hide($dialog)],
                failure_unsigned: [`음원 등록 신청`, `세션이 만료되었습니다. 로그인 후 다시 시도해 주세요.<br><br>확인 버튼을 클릭하면 로그인 페이지로 이동합니다.`,
                    ($dialog) => {Dialog.hide($dialog)
                location.reload();
                }],
                success: ['음원 등록 신청', `음원 등록 신청이 완료되었습니다. <br><br>심사 완료 후 신청한 음원이 공개 상태로 전환됩니다.<br><br>확인 버튼을 클릭하면 음원 등록 신청 내역 페이지로 이동합니다`, ($dialog) => {Dialog.hide($dialog)
                $navItems.find((x) => x.getAttribute('rel') === 'mymusic.register_history').click();
                }],
            }[response['result']] || ['오류', '서버가 알 수 없는 응답을 반환하였습니다. 잠시 후 다시 시도해 주세요.', ($dialog) => Dialog.hide($dialog)];
            Dialog.show({
                title: title,
                content: content,
                buttons: [{text: '확인', onclick: onclick}]
            });
        };
        xhr.open('POST','/music/');
        xhr.send(formData);
        Loading.show(0);
    };
}