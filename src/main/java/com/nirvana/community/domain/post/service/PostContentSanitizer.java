package com.nirvana.community.domain.post.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Cleaner;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

import com.nirvana.community.global.exception.BusinessException;
import com.nirvana.community.global.exception.ErrorCode;

// 게시글 본문 HTML에서 허용한 태그만 남긴다. (COM-PST-04, COM-NFR-04)
// 허용 목록(allowlist) 방식이라 목록에 없는 태그·속성은 모두 지워진다. script·style 안의 내용, on* 속성,
// javascript: 주소도 남지 않는다. DB를 쓰지 않는 순수 계산이라 트랜잭션 밖에서 부른다.
@Component
public class PostContentSanitizer {

	// 임시, 팀 확정 필요: 에디터(후보 TipTap)가 정해지면 그 에디터가 만드는 태그로 바꾸고 프론트와 같은 목록을 쓴다.
	// img는 일부러 넣지 않았다. 이미지 첨부(COM-PST-30)가 아직 없고 허용할 CDN 주소도 정해지지 않아 전부 지운다.
	private static final String[] ALLOWED_TAGS = {
		"p", "br",
		"strong", "b", "em", "i", "u",
		"ul", "ol", "li",
		"blockquote",
		"h1", "h2", "h3", "h4", "h5", "h6",
		"a"
	};
	private static final String[] ALLOWED_LINK_PROTOCOLS = {"http", "https"};

	private static final Safelist SAFELIST = Safelist.none()
		.addTags(ALLOWED_TAGS)
		.addAttributes("a", "href")
		.addProtocols("a", "href", ALLOWED_LINK_PROTOCOLS);

	// 정제한 본문을 돌려준다. 정제한 뒤 글자가 하나도 없으면(예: <script>만 있던 본문) 저장하지 않고 거절한다.
	public String sanitize(String content) {
		Document cleaned = new Cleaner(SAFELIST).clean(Jsoup.parseBodyFragment(content));
		if (cleaned.body().text().isBlank()) {
			throw new BusinessException(ErrorCode.POST_CONTENT_EMPTY);
		}
		// 줄바꿈·들여쓰기를 덧붙이지 않고 쓴 모양 그대로 저장한다.
		cleaned.outputSettings().prettyPrint(false);
		return cleaned.body().html();
	}
}
