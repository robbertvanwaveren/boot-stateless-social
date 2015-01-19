var app = angular.module('statelessApp', ['ngCookies']).factory('TokenStorage', function() {
	var storageKey = 'auth_token';
	return {		
		store : function(token) {
			return localStorage.setItem(storageKey, token);
		},
		retrieve : function() {
			return localStorage.getItem(storageKey);
		},
		clear : function() {
			return localStorage.removeItem(storageKey);
		}
	};
}).factory('TokenAuthInterceptor', function($q, $rootScope, TokenStorage) {
	return {
		request: function(config) {
			var authToken = TokenStorage.retrieve();
			if (authToken) {
				config.headers['X-AUTH-TOKEN'] = authToken;
			}
			return config;
		},
		responseError: function(error) {
			if (error.status === 401 || error.status === 403) {
				TokenStorage.clear();
				$rootScope.authenticated = false;
			}
			return $q.reject(error);
		}
	};
}).config(function($httpProvider) {
	$httpProvider.interceptors.push('TokenAuthInterceptor');
});

app.controller('AuthCtrl', function ($scope, $rootScope, $http, $cookies, TokenStorage) {
	$rootScope.authenticated = false;
	$scope.token; // For display purposes only
	
	$scope.init = function () {
		var authCookie = $cookies['AUTH-TOKEN'];
		if (authCookie) {
			TokenStorage.store(authCookie);
			delete $cookies['AUTH-TOKEN'];
		}
		$http.get('/api/user/current').success(function (user) {
			if (user.username) {
				$rootScope.authenticated = true;
				$scope.username = user.username;
				
				// For display purposes only
				$scope.token = JSON.parse(atob(TokenStorage.retrieve().split('.')[0]));
			}
		});
	};

	$scope.logout = function () {
		// Just clear the local storage
		TokenStorage.clear();
		$rootScope.authenticated = false;
	};
	
	$scope.getSocialDetails = function() {
		$http.get('/api/facebook/details').success(function (socialDetails) {
			$scope.socialDetails = socialDetails;
		});
	};
});