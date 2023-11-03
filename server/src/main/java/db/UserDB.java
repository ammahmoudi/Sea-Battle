package db;

import model.User;
import org.hibernate.Session;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.LinkedList;
import java.util.List;

public
class  UserDB implements DBSet<User>{

    @Override
    public
    User get(int id) {
        Session session = DBTools.getSessionFactory().openSession();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery <User> criteriaQuery = criteriaBuilder.createQuery(User.class);
        Root <User> root = criteriaQuery.from(User.class);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("id"), id));
        Query query = session.createQuery(criteriaQuery);
        List results = query.getResultList();
        session.getTransaction().commit();
        session.close();

        if (!results.isEmpty()) {
            return (User) results.get(0);
        }
        return null;
    }

  public
  User getByuserName(String userName){

        Session session = DBTools.getSessionFactory().openSession();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery <User> criteriaQuery= criteriaBuilder.createQuery(User.class);
        Root <User> root =criteriaQuery.from(User.class);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("username"),userName));
        Query query = session.createQuery(criteriaQuery);
        List results = query.getResultList();
        session.getTransaction().commit();
        session.close();

        if (!results.isEmpty()){
            return (User) results.get(0);
        }
        return null;
    }

    @Override
    public
    LinkedList <User> all() {
        Session session = DBTools.getSessionFactory().openSession();
        session.beginTransaction();
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery <User> criteriaQuery= criteriaBuilder.createQuery(User.class);

        Root <User> root =criteriaQuery.from(User.class);
        
        criteriaQuery.select(root);

        Query query = session.createQuery(criteriaQuery);

        List results = query.getResultList();

        session.getTransaction().commit();

        session.close();




        if (!results.isEmpty()){
            return (LinkedList<User>) results;
        }
        return null;
    }

    @Override
    public
    void add(User user) {
        Session session = DBTools.getSessionFactory().openSession();
        session.beginTransaction();
        session.save(user);
        session.getTransaction().commit();
        session.close();
    }

    @Override
    public
    void remove(User user) {

    }

    @Override
    public
    void update(User user) {
        Session session = DBTools.getSessionFactory().openSession();
        session.beginTransaction();
      //  user=session.get(User,user.getId());
        session.update(user);
        session.getTransaction().commit();
        session.close();

    }
}
