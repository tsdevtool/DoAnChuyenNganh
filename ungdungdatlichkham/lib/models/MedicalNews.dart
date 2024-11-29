class MedicalNews {
  int? medicalNewsId;
  String? dateTime;
  String? image; // URL of the image
  String? title;
  String? content;
  int? userId;

  MedicalNews({
    this.medicalNewsId,
    this.dateTime,
    this.image,
    this.title,
    this.content,
    this.userId,
  });

  factory MedicalNews.fromMap(Map<String, dynamic> map) {
    return MedicalNews(
      medicalNewsId: map['medical_news_id'],
      dateTime: map['date_time'],
      image: map['image'],
      title: map['title'],
      content: map['content'],
      userId: map['user_id'],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'medical_news_id': medicalNewsId,
      'date_time': dateTime,
      'image': image,
      'title': title,
      'content': content,
      'user_id': userId,
    };
  }
}
