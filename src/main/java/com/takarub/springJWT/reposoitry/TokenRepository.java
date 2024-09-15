package com.takarub.springJWT.reposoitry;

import com.takarub.springJWT.model.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token,Integer> {

    //SELECT t.*
    //FROM Token t
    //INNER JOIN User u ON t.user_id = u.id
    //WHERE t.user_id = :userId AND t.logged_out = false;
    @Query("""
    select t from Token t inner join User u on t.user.id = u.id
    where t.user.id = :userId and t.isLoggedOut = false
""")
    List<Token> findAllAccessTokensByUser(Integer userId);

    Optional<Token> findByToken(String token);


}
