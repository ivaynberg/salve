package salve.testbed;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.hibernate.Session;
import org.springframework.transaction.annotation.Transactional;

import salve.depend.Dependency;

@Entity
public class Article
{
    @Dependency
    private Session session;
    @Id
    @GeneratedValue
    private Long id;
    private String title;
    private String content;

    Article()
    {

    }

    public Article(String title)
    {
        this.title = title;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Long getId()
    {
        return id;
    }

    @Transactional
    public void save()
    {
        session.saveOrUpdate(this);
    }

    public static Collection<Article> findAll()
    {
        return new Article().session.createCriteria(Article.class).list();
    }

}
