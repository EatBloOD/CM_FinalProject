class Note(object):
    @staticmethod
    def toJson(id, group_id, username, title, body):
        note_schema = {
            'id': id,
            'groupId': group_id,
            'username': username,
            'title': title,
            'body': body
        }
        return note_schema
