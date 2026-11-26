package com.nirvana.community.domain.post.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nirvana.community.domain.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Long> {
}
