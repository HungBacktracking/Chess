
class SocketMsgModel {
    constructor({
      competitorSocketId,
      yourTurn,
      fromX,
      fromY,
      toX,
      toY,
    }) {
      this.competitorSocketId = competitorSocketId;
      this.yourTurn = yourTurn;
      this.fromX = fromX || null;
      this.fromY = fromY|| null;
      this.toX = toX|| null;
      this.toY = toY|| null;
    }
    static fromJson(json) {
      return new SocketMsgModel({
        competitorSocketId: json["competitorSocketId"] || null,
        yourTurn: json["yourTurn"] || null,
        fromX: json["fromX"] || null,
        fromY: json["fromY"] || null,
        toX: json["toX"] || null,
        toY: json["toY"] || null,
      });
    }
  }
  
  module.exports = SocketMsgModel;
  