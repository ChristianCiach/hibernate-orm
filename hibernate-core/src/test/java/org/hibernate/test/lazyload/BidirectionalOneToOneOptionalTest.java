package org.hibernate.test.lazyload;

import static org.hibernate.testing.transaction.TransactionUtil.doInJPA;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.Hibernate;
import org.hibernate.jpa.test.BaseEntityManagerFunctionalTestCase;
import org.hibernate.testing.TestForIssue;
import org.junit.Test;

public class BidirectionalOneToOneOptionalTest extends BaseEntityManagerFunctionalTestCase {

	@Override
	protected Class<?>[] getAnnotatedClasses() {
		return new Class<?>[] { Post.class, PostDetails.class, };
	}

	@Test
	@TestForIssue(jiraKey = "HHH-10771")
	public void testLazyOneToOneWithSharedPk() {
		doInJPA(this::entityManagerFactory, entityManager -> {
			Post post = new Post("First post");
			PostDetails details = new PostDetails("John Doe");
			post.setDetails(details);

			entityManager.persist(post);
		});

		doInJPA(this::entityManagerFactory, entityManager -> {
			Post post = entityManager.find(Post.class, 1L);
			assertFalse(Hibernate.isInitialized(post.getDetails()));
			post.getDetails().getCreatedBy();
			assertTrue(Hibernate.isInitialized(post.getDetails()));
		});
	}

	@Entity(name = "Post")
	@Table(name = "post")
	public static class Post {

		@Id
		@GeneratedValue
		private Long id;

		private String title;

		@OneToOne(optional = false,
				mappedBy = "post",
				cascade = CascadeType.ALL,
				fetch = FetchType.LAZY,
				orphanRemoval = true)
		private PostDetails details;

		public Post() {}

		public Post(String title) {
			this.title = title;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		public PostDetails getDetails() {
			return details;
		}

		public void setDetails(PostDetails details) {
			if (details == null) {
				if (this.details != null) {
					this.details.setPost(null);
				}
			} else {
				details.setPost(this);
			}
			this.details = details;
		}
	}

	@Entity(name = "PostDetails")
	@Table(name = "post_details")
	public static class PostDetails {

		@Id
		@GeneratedValue
		private Long id;

		@Column(name = "created_on")
		private Date createdOn;

		@Column(name = "created_by")
		private String createdBy;

		@OneToOne(fetch = FetchType.LAZY)
		@MapsId
		@JoinColumn(name = "post_id")
		private Post post;

		public PostDetails() {}

		public PostDetails(String createdBy) {
			createdOn = new Date();
			this.createdBy = createdBy;
		}

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public Date getCreatedOn() {
			return createdOn;
		}

		public String getCreatedBy() {
			return createdBy;
		}

		public Post getPost() {
			return post;
		}

		public void setPost(Post post) {
			this.post = post;
		}
	}
}
