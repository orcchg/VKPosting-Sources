package com.orcchg.vikstra.domain.sample;

import com.orcchg.vikstra.domain.model.Media;
import com.orcchg.vikstra.domain.model.Post;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PostProvider {

    @Inject
    public PostProvider() {
    }

    public Post post() {
        return Post.builder()
                .setId(1001)
                .setDescription("Thank to admins for community")
                .setTimestamp(1_456_789_101)
                .setTitle("New post one")
                .build();
    }

    public Post postWithLink() {
        return Post.builder()
                .setId(1002)
                .setDescription("Check out our link below")
                .setLink("http://yahoo.com")
                .setTimestamp(1_456_789_101)
                .setTitle("Link inside")
                .build();
    }

    public Post postWithMedia(int size) {
        return Post.builder()
                .setId(1003 + size)
                .setDescription("Kittens from Catapi")
                .setLink("http://thecatapi.com")
                .setMedia(media(size))
                .setTimestamp(1_456_789_101)
                .setTitle("Cats")
                .build();
    }

    // ------------------------------------------
    public List<Media> media(int size) {
        List<Media> list = new ArrayList<>();
        switch (size) {
            case 8:   list.add(Media.builder().setId(1008).setUrl("http://25.media.tumblr.com/tumblr_ln1xaznG951qenqklo1_1280.png").build());
            case 7:   list.add(Media.builder().setId(1007).setUrl("http://24.media.tumblr.com/tumblr_m16i6ys3jk1qzex9io1_1280.jpg").build());
            case 6:   list.add(Media.builder().setId(1006).setUrl("http://24.media.tumblr.com/tumblr_lvjkdviR2m1qzsshyo1_1280.png").build());
            case 5:   list.add(Media.builder().setId(1005).setUrl("http://24.media.tumblr.com/tumblr_lqtvnxATnw1qbt33io1_500.jpg").build());
            case 4:   list.add(Media.builder().setId(1004).setUrl("http://25.media.tumblr.com/tumblr_m5ug75DZok1qb406fo1_1280.jpg").build());
            case 3:   list.add(Media.builder().setId(1003).setUrl("http://25.media.tumblr.com/qgIb8tERiqphxdkaiUsVEFfNo1_400.jpg").build());
            default:
            case 2:   list.add(Media.builder().setId(1002).setUrl("http://25.media.tumblr.com/tumblr_lgm95mjyYV1qgnva2o1_500.jpg").build());
            case 1:   list.add(Media.builder().setId(1001).setUrl("http://25.media.tumblr.com/tumblr_m83e0hV0341qzex9io1_500.jpg").build());
                break;
        }
        return list;
    }

    // ------------------------------------------
    public List<Post> posts() {
        List<Post> list = new ArrayList<>();
        list.add(post());
        list.add(postWithLink());
        list.add(postWithMedia(1));
        list.add(postWithMedia(2));
        list.add(postWithMedia(3));
        list.add(postWithMedia(4));
        list.add(postWithMedia(5));
        list.add(postWithMedia(6));
        list.add(postWithMedia(7));
        list.add(postWithMedia(8));
        return list;
    }
}
