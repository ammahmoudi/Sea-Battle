package model;

import javax.persistence.*;

@Entity
@Table(name="users")
public
class User {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Integer id ;

    private String password;

    @Column(unique=true)
    private String username;

    public
    int getScore() {
        return Score=0;
    }

    public
    User setScore(int score) {
        Score = score;
        return this;
    }

    private int Score=0;


    public User(){}
    public
    User(String username, String password) {
        this.password = password;
        this.username = username;
    }

    public
    Integer getId() {
        return id;
    }

    public
    User setId(Integer id) {
        this.id = id;
        return this;
    }



    public
    String getPassword() {
        return password;
    }

    public
    User setPassword(String password) {
        this.password = password;
        return this;
    }

    public
    String getUsername() {
        return username;
    }

    public
    User setUsername(String userName) {
        this.username = userName;
        return this;
    }




    @Override
    public
    int hashCode() {
        return (this.getId());
    }

    @Override
    public
    boolean equals(Object obj) {

        if (!(obj instanceof User)) { return false; }

        return(this.getId().equals(((User) (obj)).getId()));
    }






}