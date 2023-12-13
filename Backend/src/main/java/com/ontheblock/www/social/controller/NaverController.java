package com.ontheblock.www.social.controller;

import com.ontheblock.www.member.JWT.JwtService;
import com.ontheblock.www.member.service.MemberService;
import com.ontheblock.www.social.domain.ResponseLoginMember;
import com.ontheblock.www.social.domain.google.GoogleUserInfo;
import com.ontheblock.www.social.domain.naver.NaverClient;
import com.ontheblock.www.social.domain.naver.NaverProfile;
import com.ontheblock.www.social.service.SocialService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequestMapping("/naver")
@RestController
@RequiredArgsConstructor
public class NaverController {

    private final NaverClient naverClient;
    private final SocialService socialService;
    private final JwtService jwtService;
    private final MemberService memberService;

    @GetMapping("/login")
    public void googleLoginOrRegister(HttpServletResponse httpServletResponse) throws Exception{

        System.out.println("네이버 로그인");
        naverClient.getAuthCode(httpServletResponse);
    }

    /* 수정전 */
    @GetMapping("/redirect")
    public ResponseEntity<?> naverRedirect(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws Exception{
        String googleToken= naverClient.getToken(httpServletRequest.getParameter("code")); // authCode로 token 요청
        System.out.println("네이버 토큰: "+googleToken);
        NaverProfile naverProfile = naverClient.getUserInfo(googleToken); // token으로 google member data 요청
        System.out.println("유저 정보:" + naverProfile.getEmail());
        ResponseLoginMember member=socialService.naverLoginOrRegister(naverProfile);

        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("id", member.getMemberId());
        tokenMap.put("nickname", member.getNickname());
        String accessToken = jwtService.createAccessToken(tokenMap); // AccessToken 생성
        String refreshToken = jwtService.createRefreshToken(tokenMap);  // RefreshToken 생성

        memberService.saveRefreshToken(member.getMemberId(), refreshToken); // 토큰 저장

        // 이동할 프론트 페이지 주소 설정
        String frontURI = naverClient.getFrontURI(member.getIsNewMember(), member.getNickname());

        // 쿠키로 보내면 자동으로 local에 저장됨.
        Cookie accessTokenCookie = new Cookie("accessToken", accessToken);
        accessTokenCookie.setHttpOnly(false);
        accessTokenCookie.setMaxAge(3600); // 쿠키 유효 시간 설정 (예: 1시간)
        accessTokenCookie.setPath("/");
        httpServletResponse.addCookie(accessTokenCookie);

        Cookie refreshTokenCookie = new Cookie("refreshToken",refreshToken);
        refreshTokenCookie.setHttpOnly(false);
        refreshTokenCookie.setMaxAge(3600);
        refreshTokenCookie.setPath("/");
        httpServletResponse.addCookie(refreshTokenCookie);

        Cookie memberIdCookie = new Cookie("memberId",member.getMemberId().toString());
        memberIdCookie.setHttpOnly(false);
        memberIdCookie.setMaxAge(3600);
        memberIdCookie.setPath("/");
        httpServletResponse.addCookie(memberIdCookie);

        return ResponseEntity
                .status(HttpStatus.FOUND) // 302
                .location(URI.create(frontURI))
                .build();
    }
}
