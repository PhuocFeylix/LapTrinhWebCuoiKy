package vn.uteexpress.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "comment_media")
public class CommentMedia {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "comment_id", nullable = false)
	private Comment comment;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private MediaType mediaType;

	@Column(nullable = false, length = 500)
	private String mediaUrl;

	public CommentMedia() {
	}

	public Long getId() {
		return id;
	}

	public Comment getComment() {
		return comment;
	}

	public void setComment(Comment comment) {
		this.comment = comment;
	}

	public MediaType getMediaType() {
		return mediaType;
	}

	public void setMediaType(MediaType mediaType) {
		this.mediaType = mediaType;
	}

	public String getMediaUrl() {
		return mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		this.mediaUrl = mediaUrl;
	}
}