package net.java.dev.moskitodemo.annotation;

import net.java.dev.moskito.annotation.callingAspect.MethodCallAspect;
import net.java.dev.moskito.core.registry.ProducerRegistryFactory;
import net.java.dev.moskitodemo.sqltrace.persistence.data.Comment;
import net.java.dev.moskitodemo.sqltrace.persistence.data.CommentBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @author <a href="mailto:vzhovtiuk@anotheria.net">Vitaliy Zhovtiuk</a>
 *         Date: 12/1/11
 *         Time: 12:48 PM
 *         To change this template use File | Settings | File Templates.
 */
public class MonitorAnnotationTest {

    public static void main(String[] a) throws CommentsPersistenceServiceException, SQLException {

        DBUtil.createTable();
        ICommentsPersistenceService service = CommentsPersistenceServiceFactory.createCommentsPersistenceService();
        Comment comment = new CommentBuilder().firstName("John").lastName("Mayal").email("mayal@gmail.com").text("Good point!").build();
        service.createComment(comment);
        Comment comment2 = new CommentBuilder().firstName("Adam").lastName("Sandle").email("asandl@gmail.com").text("Me too!").build();
        service.createComment(comment2);
        List<Comment> comments = service.getComments();
        service.getComment(comments.get(0).getId());
        for (Comment commentCur : comments) {
            service.deleteComment(commentCur.getId());
        }

        DBUtil.dropTable();
        MethodCallAspect methodCallAspect = (MethodCallAspect) ProducerRegistryFactory.getProducerRegistryInstance().getProducer(MethodCallAspect.PRODUCER_ID);

        System.out.println(methodCallAspect.toString());
    }
}