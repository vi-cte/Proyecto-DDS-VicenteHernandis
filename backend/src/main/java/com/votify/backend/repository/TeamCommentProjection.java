package com.votify.backend.repository;

import java.time.Instant;

// Proyección de comentario persistido en un voto.
public interface TeamCommentProjection {
    String getComment();
    Instant getCreatedAt();
    String getVoterRole();
}
