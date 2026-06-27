package jp.igapyon.mikumd2pptx.core;

class SlideRelationship {
    final String id;
    final String type;
    final String target;
    final String targetMode;

    SlideRelationship(String id, String type, String target) {
        this(id, type, target, null);
    }

    SlideRelationship(String id, String type, String target, String targetMode) {
        this.id = id;
        this.type = type;
        this.target = target;
        this.targetMode = targetMode;
    }
}
