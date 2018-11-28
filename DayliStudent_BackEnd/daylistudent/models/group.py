class Group(object):
    @staticmethod
    def toJson(id, name):
        group_schema = {
            'id': id,
            'name': name
        }
        return group_schema
