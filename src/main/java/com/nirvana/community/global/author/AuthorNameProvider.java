package com.nirvana.community.global.author;

import com.nirvana.community.global.auth.Requester;

// 글·댓글에 저장할 작성자 표시 이름(authorName)을 구한다. (COM-INT-01)
// 실제 값(닉네임, 운영자의 소속 사찰명)은 Member·Temple 서비스에 있어 외부 연동으로 둔다.
public interface AuthorNameProvider {

	String getAuthorName(Requester requester);
}
